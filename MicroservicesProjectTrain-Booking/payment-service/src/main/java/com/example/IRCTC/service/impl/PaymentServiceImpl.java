package com.example.IRCTC.service.impl;

import com.example.IRCTC.entity.Transaction;
import com.example.IRCTC.repository.TransactionRepository;
import com.example.IRCTC.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public Transaction processPayment(Long bookingId, BigDecimal amount) {
        logger.info("Processing payment for bookingId={}, amount={}", bookingId, amount);
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("A valid bookingId is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        java.util.Optional<Transaction> existing = transactionRepository.findByBookingId(bookingId);
        if (existing.isPresent() && "SUCCESS".equals(existing.get().getStatus())) {
            return existing.get();
        }

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
    @Transactional
    public Transaction processRefund(Long bookingId) {
        logger.info("Processing refund for bookingId={}", bookingId);
        Transaction tx = transactionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Transaction not found for booking ID: " + bookingId));
        if ("REFUNDED".equals(tx.getStatus())) {
            return tx;
        }

        tx.setStatus("REFUNDED");
        Transaction savedTx = transactionRepository.save(tx);
        logger.info("Refund successful. Transaction ID: {}", savedTx.getTransactionId());

        // Publish to Kafka
        kafkaTemplate.send("payment-events", savedTx.getTransactionId() + ":REFUNDED:" + bookingId);

        return savedTx;
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction getTransaction(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("A valid bookingId is required");
        }
        return transactionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new com.example.IRCTC.exceptions.ResourceNotFoundException(
                        "Transaction not found for booking ID: " + bookingId));
    }
}
