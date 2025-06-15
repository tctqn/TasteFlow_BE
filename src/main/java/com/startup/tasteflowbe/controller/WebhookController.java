package com.startup.tasteflowbe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.startup.tasteflowbe.config.PayOSConfig;
import com.startup.tasteflowbe.dto.PayOSWebhookDTO;
import com.startup.tasteflowbe.service.OrderService;
import com.startup.tasteflowbe.utils.PayOSWebhookUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.PaymentLinkData;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final PayOS payOS;
    private final OrderService orderService;
    private final PayOSConfig payOSConfig;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody PayOSWebhookDTO webhook) {
        System.out.println("🔔 Webhook: " + webhook);
        try {
            // Gọi lại hàm verifySignature chính xác
            boolean isValid = PayOSWebhookUtil.verifySignature(webhook, payOSConfig.getChecksumKey());

            if (!isValid) {
                System.out.println("❌ Signature mismatch");
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            Long orderCode = webhook.getData().getOrderCode();
            PaymentLinkData paymentInfo = payOS.getPaymentLinkInformation(orderCode);
            String status = paymentInfo.getStatus();

            System.out.println("✅ Webhook received for order code: " + orderCode);
            System.out.println("✅ Payment status: " + status);

            if ("PAID".equalsIgnoreCase(status)) {
                orderService.markOrderAsPaid(orderCode);
            }

            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }
}
