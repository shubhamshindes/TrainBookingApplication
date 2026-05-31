package com.example.IRCTC.controller;

import com.example.IRCTC.entity.Transaction;
import com.example.IRCTC.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/charge")
    public ResponseEntity<Transaction> charge(@RequestBody Map<String, Object> request) {
        Long bookingId = ((Number) request.get("bookingId")).longValue();
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        Transaction tx = paymentService.processPayment(bookingId, amount);
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/refund/{bookingId}")
    public ResponseEntity<Transaction> refund(@PathVariable Long bookingId) {
        Transaction tx = paymentService.processRefund(bookingId);
        return ResponseEntity.ok(tx);
    }
}
