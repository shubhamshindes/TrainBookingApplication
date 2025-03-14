package com.example.IRCTC.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.IRCTC.entity.Train;
import com.example.IRCTC.service.TrainService;

@RestController
@RequestMapping("/trains")
class TrainController {

    @Autowired
    private TrainService trainService;

    private static final Logger logger = LoggerFactory.getLogger(TrainController.class);

    @GetMapping("/allTrains")
    public ResponseEntity<Map<String, Object>> getTrains() {
        logger.info("Fetching all trains...");
        List<Train> trains = trainService.getAllTrains();

        Map<String, Object> response = new HashMap<>();
        if (trains.isEmpty()) {
            logger.warn("No trains found in the system.");
            response.put("message", "No trains available in the system.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }

        logger.info("Successfully retrieved {} trains.", trains.size());
        response.put("message", "Trains retrieved successfully.");
        response.put("data", trains);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteTrainById/{id}")
    public ResponseEntity<Map<String, Object>> deleteByTrainId(@PathVariable Long id) {
        logger.info("Attempting to delete train with ID: {}", id);
        boolean isDeleted = trainService.deleteTrainById(id);

        Map<String, Object> response = new HashMap<>();
        if (isDeleted) {
            logger.info("Train with ID {} deleted successfully.", id);
            response.put("message", "Train deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Train with ID {} not found.", id);
            response.put("message", "Train not found with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addTrain(@RequestBody Train train) {
        logger.info("Adding new train: {}", train);
        Train savedTrain = trainService.addTrain(train);

        logger.info("Train added successfully with ID: {}", savedTrain.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Train added successfully.");
        response.put("trainId", savedTrain.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
