package com.example.IRCTC.client;

import com.example.IRCTC.dto.TrainDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign Client for inter-service communication with the train-service.
 * Uses Eureka service name "TRAIN-SERVICE" for load-balanced HTTP calls.
 */
@FeignClient(name = "TRAIN-SERVICE", fallback = TrainServiceClientFallback.class)
public interface TrainServiceClient {

    /**
     * Fetch a single train by its ID.
     */
    @GetMapping("/trains/{trainId}/available-seats")
    ResponseEntity<Map<String, Object>> getTrainAvailableSeats(
            @PathVariable("trainId") Long trainId,
            @RequestHeader("Authorization") String authorization
    );

    /**
     * Fetch full train details by ID.
     */
    @GetMapping("/trains/{trainId}/details")
    ResponseEntity<TrainDto> getTrainById(
            @PathVariable("trainId") Long trainId,
            @RequestHeader("Authorization") String authorization
    );

    /**
     * Update available seat count on the train-service after a booking.
     */
    @PutMapping("/trains/{trainId}/update-seats")
    ResponseEntity<Map<String, Object>> updateAvailableSeats(
            @PathVariable("trainId") Long trainId,
            @RequestParam("seats") int seats,
            @RequestHeader("Authorization") String authorization
    );
}
