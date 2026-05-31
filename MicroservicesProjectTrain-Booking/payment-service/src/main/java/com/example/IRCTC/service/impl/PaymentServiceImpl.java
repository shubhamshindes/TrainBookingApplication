package com.example.IRCTC.service.impl;

import com.example.IRCTC.entity.Transaction;
import com.example.IRCTC.repository.TransactionRepository;
import com.example.IRCTC.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Override
    public Transaction processPayment(Long bookingId, BigDecimal amount) {
        logger.info("Processing payment for bookingId={}, amount={}", bookingId, amount);

        Transaction transaction = new Transaction();
        transaction.setBookingId(bookingId);
        transaction.setAmount(amount);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setStatus("SUCCESS");

        Transaction savedTx = transactionRepository.save(transaction);
        logger.info("Payment successful. Transaction ID: {}", savedTx.getTransactionId());

        // Publish to Kafka
        kafkaTemplate.send("payment-events", savedTx.getTransactionId() + ":SUCCESS:" + bookingId);

        return savedTx;
    }

    @Override
    public Transaction processRefund(Long bookingId) {
        logger.info("Processing refund for bookingId={}", bookingId);
        Transaction tx = transactionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Transaction not found for booking ID: " + bookingId));

        tx.setStatus("REFUNDED");
        Transaction savedTx = transactionRepository.save(tx);
        logger.info("Refund successful. Transaction ID: {}", savedTx.getTransactionId());

        // Publish to Kafka
        kafkaTemplate.send("payment-events", savedTx.getTransactionId() + ":REFUNDED:" + bookingId);

        return savedTx;
    }
}
