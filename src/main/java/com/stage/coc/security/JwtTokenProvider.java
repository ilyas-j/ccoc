package com.stage.coc.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwtSecret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs:604800000}")
    private int jwtExpirationInMs;

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + jwtExpirationInMs);

        // Créer un simple token avec userId et expiration
        String payload = userPrincipal.getId() + ":" + expiryDate.getTime();
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public Long getUserIdFromJWT(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    public boolean validateToken(String authToken) {
        try {
            String decoded = new String(Base64.getDecoder().decode(authToken), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");

            if (parts.length != 2) {
                return false;
            }

            long expirationTime = Long.parseLong(parts[1]);
            return System.currentTimeMillis() < expirationTime;

        } catch (Exception ex) {
            System.err.println("Invalid JWT token: " + ex.getMessage());
            return false;
        }
    }
}