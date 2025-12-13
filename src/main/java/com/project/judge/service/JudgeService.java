package com.project.judge.service;

import com.project.judge.dto.request.SimpleSubmissionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgeService {

    @Value("${judge0.url}")
    private String judgeUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String checkConnection(){
        try {
            String url = judgeUrl + "/about";

            log.info("Checking connection to judge");

            String response = restTemplate.getForObject(url, String.class);

            log.info("Judge response: {}", response);

            return response;

        }catch (Exception e){
            log.error("Failed to connect to judge", e);
            return "Connection Failed" + e.getMessage();
        }
    }

    public Map<String, Object> submitCode(SimpleSubmissionRequest request){
        try {
            String url = judgeUrl + "/submissions?base64_encoded=false&wait=true";

            String jsonBody = objectMapper.writeValueAsString(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            System.out.println(requestEntity.getBody());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> result = response.getBody();

            return result;

        }catch (Exception e){
            log.error("Failed to submit the code to judge", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return error;
        }
    }

}
