package com.example.IRCTC.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.IRCTC.entity.Train;
import com.example.IRCTC.repository.TrainRepository;

@Service
public class TrainService {

    @Autowired
    private TrainRepository trainRepository;

    private static final Logger logger = LoggerFactory.getLogger(TrainService.class);

    public List<Train> getAllTrains() {
        logger.info("Fetching all trains from the database...");
        List<Train> trains = trainRepository.findAll();
        logger.info("Total trains found: {}", trains.size());
        return trains;
    }

    public Train addTrain(Train train) {
        logger.info("Saving new train: {}", train);
        Train savedTrain = trainRepository.save(train);
        logger.info("Train saved successfully with ID: {}", savedTrain.getId());
        return savedTrain;
    }

    public boolean deleteTrainById(Long id) {
        logger.info("Attempting to delete train with ID: {}", id);
        if (trainRepository.existsById(id)) {
            trainRepository.deleteById(id);
            logger.info("Train with ID {} deleted successfully.", id);
            return true; // Successfully deleted
        }
        logger.warn("Train with ID {} not found.", id);
        return false; // Train ID not found
    }
}
