package com.example.IRCTC.service;

import java.util.List;

import com.example.IRCTC.exceptions.TrainNotFoundException;
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

        public Train getTrainById(Long trainId) {
            return trainRepository.findById(trainId)
                    .orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + trainId));
        }
        public void deleteTrainById(Long id) {
            Train train = trainRepository.findById(id)
                    .orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + id));

            trainRepository.delete(train);
            logger.info("Train with ID {} deleted successfully.", id);
        }
    }
