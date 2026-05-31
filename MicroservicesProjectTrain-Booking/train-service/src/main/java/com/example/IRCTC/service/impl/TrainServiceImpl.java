package com.example.IRCTC.service.impl;

import com.example.IRCTC.entity.Train;
import com.example.IRCTC.exceptions.TrainNotFoundException;
import com.example.IRCTC.repository.TrainRepository;
import com.example.IRCTC.service.TrainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TrainServiceImpl implements TrainService {

    @Autowired
    private TrainRepository trainRepository;

    private static final Logger logger = LoggerFactory.getLogger(TrainServiceImpl.class);

    @Override
    public List<Train> getAllTrains() {
        logger.info("Fetching all trains...");
        return trainRepository.findAll();
    }

    @Override
    public Train addTrain(Train train) {
        logger.info("Adding a new train to the system...");
        return trainRepository.save(train);
    }

    @Override
    public Train getTrainById(Long trainId) {
        logger.info("Fetching train details for ID: {}", trainId);
        return trainRepository.findById(trainId)
                .orElseThrow(() -> {
                    logger.error("Train not found with ID: {}", trainId);
                    return new TrainNotFoundException("Train not found with ID: " + trainId);
                });
    }

    @Override
    public void deleteTrainById(Long id) {
        logger.info("Checking if train exists with ID: {}", id);
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + id));

        trainRepository.delete(train);
        logger.info("Train with ID: {} deleted successfully.", id);
    }

    @Override
    public void updateSeats(Long trainId, int seats) {
        logger.info("Updating seats for trainId={} with delta={}", trainId, seats);
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + trainId));

        int newSeats = train.getAvailableSeats() + seats;
        if (newSeats < 0) {
            logger.error("Seat update failed. Negative seat value encountered for trainId={}", trainId);
            throw new IllegalArgumentException("Seats cannot be less than zero!");
        }

        train.setAvailableSeats(newSeats);
        trainRepository.save(train);
        logger.info("Seats updated successfully. New availability for trainId={} is {}", trainId, newSeats);
    }
}
