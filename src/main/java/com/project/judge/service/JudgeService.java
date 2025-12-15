package com.project.judge.service;

import com.project.judge.config.Judge0Properties;
import com.project.judge.dto.request.JudgeSubmissionRequest;
import com.project.judge.dto.response.JudgeSubmissionResponse;
import com.project.judge.exception.JudgeException;
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
            throw new JudgeException("Failed to get Judge0 info: ");
        }
    }

    @Retryable(
            value = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public JudgeSubmissionResponse submitAndWait(JudgeSubmissionRequest request){
        try{
            String url = properties.getUrl() + "/submission?base64_encoded=false&wait=true";

            log.info("Submitting to Judge: languageId={}, codeLength{}",
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
            throw new JudgeException("Judge0 client error: ");
        } catch (HttpServerErrorException e) {
            log.error("Judge0 server error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new JudgeException("Judge0 server error: ");
        } catch (Exception e) {
            log.error("Failed to submit to Judge0", e);
            throw new JudgeException("Failed to submit to Judge0: ");
        }
    }



    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (properties.getApiKey() != null && !properties.getApiKey().isEmpty()) {
            headers.set("X-Auth-Token", properties.getApiKey());
        }

        return headers;
    }
}
