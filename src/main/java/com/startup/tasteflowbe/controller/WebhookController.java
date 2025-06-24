package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.config.PayOSConfig;
import com.startup.tasteflowbe.service.OrderService;
import com.startup.tasteflowbe.service.ProductService;
import com.startup.tasteflowbe.utils.PayOSWebhookUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final PayOSConfig payOSConfig;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> handleWebhook(@RequestBody String body) {
        try {
            JSONObject root = new JSONObject(body);
            JSONObject data = root.getJSONObject("data");
            String signature = root.getString("signature");

            if (data.has("description") && "VQRIO123".equals(data.getString("description"))) {
                System.out.println("Webhook mặc định từ PayOS, bỏ qua.");
                return ResponseEntity.ok().build();
            }

            boolean valid = PayOSWebhookUtil.isValidData(data, signature, payOSConfig.getChecksumKey());
            if (valid) {
                Long orderCode = data.getLong("orderCode") ;
                orderService.markOrderAsPaid(orderCode);
            } else {
                System.out.println("Webhook sai chữ ký!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

}
