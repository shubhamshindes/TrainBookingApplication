package com.example.IRCTC.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void consumePaymentEvent(String message) {
        logger.info("Consumed payment event: {}", message);
        String[] parts = message.split(":");
        String txId = parts[0];
        String status = parts[1];
        String bookingId = parts[2];

        if (status.equals("SUCCESS")) {
            sendEmailSimulation(bookingId, "Your ticket booking was successful! Transaction: " + txId);
            sendSmsSimulation(bookingId, "Ticket confirmed successfully!");
        } else if (status.equals("REFUNDED")) {
            sendEmailSimulation(bookingId, "Your cancellation refund has been processed! Transaction: " + txId);
        }
    }

    private void sendEmailSimulation(String bookingId, String body) {
        logger.info("[EMAIL SIMULATION] Sending Email to User for bookingId={}. Body: {}", bookingId, body);
    }

    private void sendSmsSimulation(String bookingId, String body) {
        logger.info("[SMS SIMULATION] Sending SMS to User for bookingId={}. Body: {}", bookingId, body);
    }
}
