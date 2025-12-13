package com.project.judge.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class IdGenerator {
    private static final String charSet = "AB0CD1EF2GH3IK4LM5NO6PQ7RS8TU9VW0XY1Z";
    private static final SecureRandom random = new SecureRandom();

    public static String generateId(String prefix, int length) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);

        for (int i = 0; i < length; i++) {
            sb.append(charSet.charAt(random.nextInt(charSet.length())));
        }
        return sb.toString();
    }
}
