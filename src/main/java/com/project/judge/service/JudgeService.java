package com.project.judge.service;

import com.project.judge.config.Judge0Properties;
import com.project.judge.dto.request.JudgeBatchRequest;
import com.project.judge.dto.request.JudgeSubmissionRequest;
import com.project.judge.dto.response.JudgeBatchResponse;
import com.project.judge.dto.response.JudgeSubmissionResponse;
import com.project.judge.exception.JudgeException;
import com.project.judge.model.TestCase;
import com.project.judge.constant.JudgeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgeService {

    private final RestTemplate restTemplate;
    private final Judge0Properties properties;

    public boolean isAvailable(){
        try{
            String url = properties.getUrl() + "/about";
            ResponseEntity<String> response = restTemplate.getForEntity(url,String.class);
            return response.getStatusCode().is2xxSuccessful();

        }catch(Exception e){
            log.warn("Judge0 is not available: {}", e.getMessage());
            return false;
        }
    }

    public String getAbout() {
        try {
            String url = properties.getUrl() + "/about";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get Judge0 info", e);
            throw new JudgeException("Failed to get Judge0 info: " + e.getMessage());
        }
    }

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public JudgeSubmissionResponse submitAndWait(JudgeSubmissionRequest request){
        try{
            String url = properties.getUrl() + "/submissions?base64_encoded=false&wait=true";

            log.info("Submitting to Judge: languageId={}, codeLength={}",
                    request.getLanguageId(),
                    request.getSourceCode() != null ? request.getSourceCode().length() : 0);

            HttpHeaders headers = createHeaders();
            HttpEntity<JudgeSubmissionRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<JudgeSubmissionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    JudgeSubmissionResponse.class
            );

            JudgeSubmissionResponse result = response.getBody();

            if (result != null) {
                log.info("Judge0 response: token={}, status={}, time={}s, memory={}KB",
                        result.getToken(),
                        result.getStatus() != null ? result.getStatus().getDescription() : "null",
                        result.getTime(),
                        result.getMemory());
            }

            return result;

        } catch (HttpClientErrorException e) {
            log.error("Judge0 client error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new JudgeException("Judge0 client error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Judge0 server error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new JudgeException("Judge0 server error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to submit to Judge0", e);
            throw new JudgeException("Failed to submit to Judge0: " + e.getMessage());
        }
    }


    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public JudgeSubmissionResponse getSubmission(String token){
        try{
            String url = properties.getUrl() + "/submission/" + token + "?base64_encoded=false";

            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JudgeSubmissionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JudgeSubmissionResponse.class
            );

            return response.getBody();

        }catch(Exception e){
            log.error("Failed to get submission: token={}", token, e);
            throw new JudgeException("Failed to get submission");
        }
    }


    public List<JudgeSubmissionResponse> submitWithTestCases(
            String sourceCode,
            Integer languageId,
            List<TestCase> testCases,
            Integer timeLimit,
            Integer memoryLimit){

        log.info("Running code against {} test cases", testCases.size());

        List<JudgeSubmissionResponse> result = new ArrayList<>();

        for(int i =0; i< testCases.size();i++){
            TestCase testCase = testCases.get(i);

            log.info("Running test case {}/{}", i+1, testCases.size());

            JudgeSubmissionRequest request = JudgeSubmissionRequest.builder()
                    .sourceCode(sourceCode)
                    .languageId(languageId)
                    .stdin(testCase.getInput())
                    .expectedOutput(testCase.getExpectedOutput())
                    .cpuTimeLimit(timeLimit !=null ? timeLimit.doubleValue() : properties.getDefaultCpuTimeLimit())
                    .memoryLimit(memoryLimit != null ? memoryLimit : properties.getDefaultMemoryLimit())
                    .wallTimeLimit(properties.getDefaultWallTimeLimit())
                    .build();

            JudgeSubmissionResponse response = submitAndWait(request);
            result.add(response);

            if(response.getStatus() != null){
                boolean passed = JudgeStatus.isAccepted(response.getStatus().getId());
                log.info("Test case {}/{} : {} ({})",
                        i+1, testCases.size(),
                        passed ? "PASSED" : "FAILED",
                        response.getStatus().getDescription());
            }
        }

        long passedCount = result.stream()
                .filter(r -> r.getStatus() != null && JudgeStatus.isAccepted(r.getStatus().getId()))
                .count();

        log.info("Completed: {}/{} test cases passed", passedCount, testCases.size());

        return result;
    }

    /**
    *Method for submitting multiple requests
    */
    public List<JudgeSubmissionResponse> submitBatch(List<JudgeSubmissionRequest> requests) {
        try {
            String url = properties.getUrl() + "/submissions/batch?base64_encoded=false";

            JudgeBatchRequest batchRequest = new JudgeBatchRequest();
            batchRequest.setSubmissions(requests);

            log.info("Submitting batch of {} submissions", requests.size());

            HttpHeaders headers = createHeaders();
            HttpEntity<JudgeBatchRequest> entity = new HttpEntity<>(batchRequest, headers);

            ResponseEntity<JudgeBatchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    JudgeBatchResponse.class
            );

            JudgeBatchResponse batchResponse = response.getBody();

            if (batchResponse != null && batchResponse.getSubmissions() != null) {
                List<String> tokens = batchResponse.getSubmissions().stream()
                        .map(JudgeSubmissionResponse::getToken)
                        .collect(Collectors.toList());

                log.info("Batch created with {} tokens", tokens.size());

                // Wait and fetch results
                return tokens.stream()
                        .map(this::waitAndGetSubmission)
                        .collect(Collectors.toList());
            }

            return new ArrayList<>();

        } catch (Exception e) {
            log.error("Failed to submit batch", e);
            throw new JudgeException("Failed to submit batch: " + e.getMessage());
        }
    }


    public JudgeSubmissionRequest createRequest(
            String sourceCode,
            Integer languageId,
            String stdin,
            String expectedOutput) {

        return JudgeSubmissionRequest.builder()
                .sourceCode(sourceCode)
                .languageId(languageId)
                .stdin(stdin != null ? stdin : "")
                .expectedOutput(expectedOutput)
                .cpuTimeLimit(properties.getDefaultCpuTimeLimit())
                .memoryLimit(properties.getDefaultMemoryLimit())
                .wallTimeLimit(properties.getDefaultWallTimeLimit())
                .build();
    }

    /**
     * Wait for submission result (with polling)
     */
    private JudgeSubmissionResponse waitAndGetSubmission(String token) {
        int maxAttempts = 20;
        int attempt = 0;
        int delayMs = 500;

        while (attempt < maxAttempts) {
            JudgeSubmissionResponse response = getSubmission(token);

            if (response.getStatus() != null && !JudgeStatus.isProcessing(response.getStatus().getId())) {
                return response;
            }

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JudgeException("Interrupted while waiting for result");
            }

            attempt++;
        }

        throw new JudgeException("Timeout waiting for submission result: " + token);
    }



    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (properties.getApiKey() != null && !properties.getApiKey().isEmpty()) {
            headers.set("X-Api-Token", properties.getApiKey());
        }

        return headers;
    }
}
