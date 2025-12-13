package com.project.judge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgeService {

    @Value("${judge0.url}")
    private String judgeUrl;

    private final RestTemplate restTemplate;

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

}
