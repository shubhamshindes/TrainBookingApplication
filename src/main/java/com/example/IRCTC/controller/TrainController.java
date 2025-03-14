package com.example.IRCTC.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getTrains() {
        List<Train> trains = trainService.getAllTrains();
        if (trains.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "No trains available in the system.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trains retrieved successfully.");
        response.put("data", trains);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteTrainById/{id}")
    public ResponseEntity<Map<String, Object>> deleteByTrainId(@PathVariable Long id) {
        boolean isDeleted = trainService.deleteTrainById(id);

        Map<String, Object> response = new HashMap<>();
        if (isDeleted) {
            response.put("message", "Train deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Train not found with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addTrain(@RequestBody Train train) {
        Train savedTrain = trainService.addTrain(train);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Train added successfully.");
        response.put("trainId", savedTrain.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
