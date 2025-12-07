package com.project.judge.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.project.judge.dto.response.CodeExecutionResponse;
import com.project.judge.exception.JudgeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JudgeService {

    private static final long MEMORY_LIMIT = 512 * 1024 * 1024L; // 512MB
    private static final long TIMEOUT_SECONDS = 5L;
    private static final int MAX_OUTPUT_SIZE = 50000; // 50KB
    private static final int MAX_ERROR_SIZE = 10000;  // 10KB

    private final DockerClient dockerClient;

    public JudgeService() {
        log.info("---> Initializing Docker client...");

        try {
            DefaultDockerClientConfig config = DefaultDockerClientConfig
                    .createDefaultConfigBuilder()
                    .build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();

            this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
            dockerClient.pingCmd().exec();
            log.info("---> Docker connection successful!");

            pullPythonImage();

        } catch (Exception e) {
            log.error("---> Failed to connect to Docker: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to Docker", e);
        }
    }

    private void pullPythonImage() {
        try {
            log.info("---> Checking Python image...");

            boolean imageExists = dockerClient.listImagesCmd()
                    .withImageNameFilter("python:3.11-alpine")
                    .exec()
                    .stream()
                    .findAny()
                    .isPresent();

            if (!imageExists) {
                log.info("---> Pulling python:3.11-alpine...");
                dockerClient.pullImageCmd("python:3.11-alpine")
                        .start()
                        .awaitCompletion();
                log.info("---> Python image ready!");
            } else {
                log.info("---> Python image already available!");
            }
        } catch (Exception e) {
            log.error("---> Warning: Could not pull Python image: {}", e.getMessage());
        }
    }

    public CodeExecutionResponse executeCode(String code) {
        return executeCode(code, null);
    }

    public CodeExecutionResponse executeCode(String code, String input) {
        log.info("---> Executing Python code...");

        Path tempDir = null;
        Path codeFile = null;
        String containerId = null;

        try {
            // 1. Prepare code file
            code = code.strip(); // Remove leading/trailing whitespace
            tempDir = Files.createTempDirectory("judge_");
            codeFile = tempDir.resolve("solution.py");
            Files.writeString(codeFile, code, StandardCharsets.UTF_8);

            log.info("---> Temp file: {}", codeFile);

            // 2. Create container
            CreateContainerResponse container = dockerClient
                    .createContainerCmd("python:3.11-alpine")
                    .withCmd("python", "-u", "/app/solution.py") // -u for unbuffered output
                    .withHostConfig(new HostConfig()
                            .withMemory(MEMORY_LIMIT)
                            .withMemorySwap(MEMORY_LIMIT) // Prevent swap usage
                            .withCpuQuota(50000L)
                            .withCpuPeriod(100000L)
                            .withPidsLimit(50L) // Limit processes
                            .withNetworkMode("none")
                            .withBinds(new Bind(
                                    tempDir.toString(),
                                    new Volume("/app"),
                                    AccessMode.ro
                            ))
                            .withAutoRemove(true))
                    .exec();

            containerId = container.getId();
            log.info("---> Container created: {}", containerId.substring(0, 12));

            // 3. Start container
            dockerClient.startContainerCmd(containerId).exec();
            log.info("---> Container started");

            // 4. Wait for completion with timeout
            WaitContainerResultCallback callback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(containerId).exec(callback);

            Integer exitCode;
            try {
                exitCode = callback.awaitStatusCode(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.info("---> Execution completed with exit code: {}", exitCode);
            } catch (Exception e) {
                log.error("---> Timeout - killing container");
                try {
                    dockerClient.killContainerCmd(containerId).exec();
                } catch (Exception killEx) {
                    log.warn("Failed to kill container: {}", killEx.getMessage());
                }
                return new CodeExecutionResponse(false, "Time Limit Exceeded", "");
            }

            // 5. Get output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .exec(new ResultCallback.Adapter<>() {
                        @Override
                        public void onNext(Frame frame) {
                            byte[] payload = frame.getPayload();
                            try {
                                if (frame.getStreamType() == StreamType.STDOUT) {
                                    outputStream.write(payload);
                                } else if (frame.getStreamType() == StreamType.STDERR) {
                                    errorStream.write(payload);
                                }
                            } catch (Exception e) {
                                log.error("Error reading frame: {}", e.getMessage());
                            }
                        }
                    })
                    .awaitCompletion();

            String output = truncateOutput(outputStream.toString(StandardCharsets.UTF_8), MAX_OUTPUT_SIZE);
            String error = truncateOutput(errorStream.toString(StandardCharsets.UTF_8), MAX_ERROR_SIZE);

            log.info("---> Output length: {} bytes", output.length());
            if (!error.isEmpty()) {
                log.error("---> Error: {}", error);
            }

            // 6. Determine result
            if (exitCode != 0) {
                String errorMessage = !error.isEmpty() ? error : output;
                return new CodeExecutionResponse(false, errorMessage, "");
            }

            return new CodeExecutionResponse(true, output, "");

        } catch (Exception e) {
            log.error("---> Execution error: {}", e.getMessage(), e);

            // Try to clean up container if it exists
            if (containerId != null) {
                try {
                    dockerClient.killContainerCmd(containerId).exec();
                } catch (Exception killEx) {
                    // Ignore clean up errors
                }
            }

            throw new JudgeException("JUDGE_SERVICE_ERROR", "Unexpected internal service error occurred");

        } finally {
            // Always cleanup temp files
            cleanupTempFiles(codeFile, tempDir);
        }
    }

    private String truncateOutput(String output, int maxSize) {
        if (output.length() > maxSize) {
            return output.substring(0, maxSize) + "\n... (output truncated)";
        }
        return output;
    }

    private void cleanupTempFiles(Path codeFile, Path tempDir) {
        try {
            if (codeFile != null) {
                Files.deleteIfExists(codeFile);
            }
            if (tempDir != null) {
                Files.deleteIfExists(tempDir);
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup temp files: {}", e.getMessage());
        }
    }
}