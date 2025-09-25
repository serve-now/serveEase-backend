package com.servease.demo.util;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    //토큰 생성 및 유효성 검사해야함

    public String generateToken(String loginId) {
        return "jwt-token-for-" + loginId;
    }

    // public boolean validateToken(String token) { ... }
    // public String getLoginIdFromToken(String token) { ... }
}