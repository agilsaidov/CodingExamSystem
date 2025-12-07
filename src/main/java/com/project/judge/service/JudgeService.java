package com.project.judge.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.project.judge.dto.response.CodeExecutionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JudgeService {

    private final DockerClient dockerClient;

    public JudgeService() {
        log.info("---> Initializing Docker client...");

        try {
            // Create config
            DefaultDockerClientConfig config = DefaultDockerClientConfig
                    .createDefaultConfigBuilder()
                    .build();

            // Create HTTP client
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();

            // Create Docker client
            this.dockerClient = DockerClientImpl.getInstance(config, httpClient);

            // Test connection
            dockerClient.pingCmd().exec();
            log.info("---> Docker connection successful!");

            // Pull Python image if not exists
            pullPythonImage();

        } catch (Exception e) {
            log.error("---> Failed to connect to Docker: {}" , e.getMessage());
            throw new RuntimeException("Cannot connect to Docker", e);
        }
    }

    private void pullPythonImage() {
        try {
            log.info("---> Checking Python image...");

            // Check if image exists
            boolean imageExists = dockerClient.listImagesCmd()
                    .withImageNameFilter("python:3.11-alpine")
                    .exec()
                    .stream()
                    .findAny()
                    .isPresent();

            if (!imageExists) {
                log.info("---> Pulling python:3.11-alpine (this may take a minute)...");
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
        log.info("---> Executing Python code...");

        try {
            // 1. Create temp directory and code file
            Path tempDir = Files.createTempDirectory("judge_");
            Path codeFile = tempDir.resolve("solution.py");
            Files.writeString(codeFile, code);

            log.info("---> Temp file: {}", codeFile);

            // 2. Create Docker container
            CreateContainerResponse container = dockerClient
                    .createContainerCmd("python:3.11-alpine")
                    .withCmd("python", "/app/solution.py")
                    .withHostConfig(new HostConfig()
                            .withMemory(256 * 1024 * 1024L)    // 256MB RAM
                            .withCpuQuota(50000L)              // 50% CPU (2 containers per core)
                            .withNetworkMode("none")              // No internet
                            .withBinds(new Bind(
                                    tempDir.toString(),
                                    new Volume("/app"),
                                    AccessMode.ro
                            ))
                            .withAutoRemove(true))               // Auto cleanup
                    .exec();

            String containerId = container.getId();
            log.info("---> Container created: {}", containerId.substring(0, 12));

            // 3. Start container
            dockerClient.startContainerCmd(containerId).exec();
            log.info("---> Container started");

            // 4. Wait for completion (5 second timeout)
            WaitContainerResultCallback callback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(containerId).exec(callback);

            Integer exitCode;

            try {
                exitCode = callback.awaitStatusCode(5, TimeUnit.SECONDS);
                log.info("---> Execution completed with exit code: {}", exitCode);

            } catch (Exception e) {
                dockerClient.killContainerCmd(containerId).exec();
                log.error("---> Timeout - container killed");

                Files.deleteIfExists(codeFile);
                Files.deleteIfExists(tempDir);

                return new CodeExecutionResponse(false, "TIME_LIMIT_EXCEEDED", "");
            }

            // 5. Get output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            byte[] payload = frame.getPayload();
                            if (frame.getStreamType() == StreamType.STDOUT) {
                                try {
                                    outputStream.write(payload);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (frame.getStreamType() == StreamType.STDERR) {
                                try {
                                    errorStream.write(payload);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    })
                    .awaitCompletion();

            String output = outputStream.toString().trim();
            String error = errorStream.toString().trim();

            log.info("---> Output: {}", output);

            if (!error.isEmpty()) {
                log.error("---> Error: {}", error);
            }

            // 6. Cleanup temp files
            Files.deleteIfExists(codeFile);
            Files.deleteIfExists(tempDir);

            // 7. Return result
            if (exitCode != 0) {
                return new CodeExecutionResponse(false, error, output);
            }

            return new CodeExecutionResponse(true, output, "");

        } catch (Exception e) {
            log.error("---> Execution error: {}", e.getMessage());
            return new CodeExecutionResponse(false, "RUNTIME_ERROR: " + e.getMessage(), "");
        }
    }

}