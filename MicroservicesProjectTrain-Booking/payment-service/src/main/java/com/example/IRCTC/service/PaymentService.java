package com.example.IRCTC.service;

import com.example.IRCTC.entity.Transaction;
import java.math.BigDecimal;

public interface PaymentService {
    Transaction processPayment(Long bookingId, BigDecimal amount);
    Transaction processRefund(Long bookingId);
}
