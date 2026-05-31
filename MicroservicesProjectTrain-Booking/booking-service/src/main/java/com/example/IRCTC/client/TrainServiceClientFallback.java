package com.example.IRCTC.client;

import com.example.IRCTC.dto.TrainDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback implementation for TrainServiceClient.
 * Called automatically when the train-service is down or returns an error.
 */
@Component
public class TrainServiceClientFallback implements TrainServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(TrainServiceClientFallback.class);

    @Override
    public ResponseEntity<Map<String, Object>> getTrainAvailableSeats(Long trainId) {
        logger.error("FALLBACK: train-service is unavailable. Cannot fetch seats for trainId={}", trainId);
        return ResponseEntity.status(503).body(Map.of(
                "error", "Train service is currently unavailable. Please try again later."
        ));
    }

    @Override
    public ResponseEntity<TrainDto> getTrainById(Long trainId) {
        logger.error("FALLBACK: train-service is unavailable. Cannot fetch train with id={}", trainId);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateAvailableSeats(Long trainId, int seats) {
        logger.error("FALLBACK: train-service unavailable. Cannot update seats for trainId={}", trainId);
        return ResponseEntity.status(503).body(Map.of(
                "error", "Train service is currently unavailable."
        ));
    }
}
