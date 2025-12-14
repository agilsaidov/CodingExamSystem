package com.project.judge.service;

import com.project.judge.config.Judge0Properties;
import com.project.judge.exception.Judge0Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
            throw new Judge0Exception("Failed to get Judge0 info: " + e.getMessage(), e);
        }
    }
}
