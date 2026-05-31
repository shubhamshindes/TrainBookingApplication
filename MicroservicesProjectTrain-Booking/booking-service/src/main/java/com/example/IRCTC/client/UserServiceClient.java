package com.example.IRCTC.client;

import com.example.IRCTC.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client for inter-service communication with the user-service.
 * Uses Eureka service name "USER-SERVICE" for load-balanced HTTP calls.
 */
@FeignClient(name = "USER-SERVICE", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * Fetch a user by ID from the user-service.
     */
    @GetMapping("/user/getUserById/{id}")
    ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") Long id);
}
