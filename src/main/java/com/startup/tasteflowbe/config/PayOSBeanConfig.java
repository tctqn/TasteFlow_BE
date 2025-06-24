package com.startup.tasteflowbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSBeanConfig {
    @Bean
    public PayOS payOS(PayOSConfig config) {
        return new PayOS(config.getClientId(), config.getApiKey(), config.getChecksumKey());
    }
}