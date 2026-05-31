package com.example.IRCTC.client;

import com.example.IRCTC.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback implementation for UserServiceClient.
 * Called automatically when the user-service is down or returns an error.
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public ResponseEntity<Map<String, Object>> getUserById(Long id) {
        logger.error("FALLBACK: user-service is unavailable. Cannot fetch user with id={}", id);
        return ResponseEntity.status(503).body(Map.of(
                "error", "User service is currently unavailable. Please try again later."
        ));
    }
}
