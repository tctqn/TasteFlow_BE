package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.model.Payment;
import java.util.List;

public interface PaymentService {
    List<Payment> getAllPayments();
    Payment getPaymentById(Long id);
    CreatePaymentResponseDTO createPayment(Long orderId, Long amount, String description);
    Payment updatePayment(Long id, Payment payment);
    void deletePayment(Long id);
}
