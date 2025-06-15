package com.startup.tasteflowbe.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSBeanConfig {
    @Bean
    public PayOS payOS(PayOSConfig config) {
        PayOS payOS = new PayOS(config.getClientId(), config.getApiKey(), config.getChecksumKey());

        try {
            String webhookUrl = "https://50ea-42-114-142-161.ngrok-free.app/api/webhook";
            payOS.confirmWebhook(webhookUrl);
            System.out.println("✅ Webhook URL registered successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to register webhook: " + e.getMessage());
        }

        return payOS;
    }
}
