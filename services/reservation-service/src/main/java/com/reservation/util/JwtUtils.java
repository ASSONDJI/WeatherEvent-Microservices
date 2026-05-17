package com.reservation.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractUserId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return "anonymous";
            }
            String token = authHeader.substring(7);
            String payload = token.split("[.]")[1];
            int padding = payload.length() % 4;
            if (padding > 0) payload += "=".repeat(4 - padding);
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            Map<?, ?> claims = objectMapper.readValue(decoded, Map.class);
            String userId = (String) claims.get("preferred_username");
            if (userId == null) userId = (String) claims.get("sub");
            if (userId == null) userId = "anonymous";
            return userId;
        } catch (Exception e) {
            log.warn("Could not extract userId from JWT: {}", e.getMessage());
            return "anonymous";
        }
    }

    public String extractEmail(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
            String token = authHeader.substring(7);
            String payload = token.split("[.]")[1];
            int padding = payload.length() % 4;
            if (padding > 0) payload += "=".repeat(4 - padding);
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            Map<?, ?> claims = objectMapper.readValue(decoded, Map.class);
            return (String) claims.get("email");
        } catch (Exception e) {
            log.warn("Could not extract email from JWT: {}", e.getMessage());
            return null;
        }
    }
}
