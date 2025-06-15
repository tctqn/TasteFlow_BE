package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.config.PayOSConfig;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.model.Payment;
import com.startup.tasteflowbe.repository.PaymentRepository;
import com.startup.tasteflowbe.service.PaymentService;
import com.startup.tasteflowbe.utils.OrderCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    private PaymentRepository paymentRepository;
    private final PayOS payOS;
    private final PayOSConfig config;

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment getPaymentById(Long id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.orElse(null);
    }

    @Override
    public CreatePaymentResponseDTO createPayment(Long orderCode, Long amount, String description) {
        try {
            Integer amountInVND = amount.intValue();

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(Integer.parseInt("2000"))
                    .description(description)
                    .cancelUrl(config.getCancelUrl())
                    .returnUrl(config.getReturnUrl())
                    .build();

            System.out.println("Creating payment link with data: " + paymentData);

            CheckoutResponseData response = payOS.createPaymentLink(paymentData);
            return CreatePaymentResponseDTO.builder()
                    .orderCode(response.getOrderCode().toString())
                    .status(response.getStatus())
                    .amount(response.getAmount().longValue())
                    .checkoutUrl(response.getCheckoutUrl())
                    .qrCode(response.getQrCode())
                    .paymentNote(response.getDescription() + " #" + orderCode)
                    .bankName("BIDV")
                    .accountName("NGUYEN QUANG TUYEN")
                    .accountNumber("V3CAS8854795351")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error while creating payment link: " + e.getMessage(), e);
        }
    }



    @Override
    public Payment updatePayment(Long id, Payment payment) {
        if (paymentRepository.existsById(id)) {
            payment.setPaymentId(id);
            return paymentRepository.save(payment);
        }
        return null;
    }

    @Override
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
