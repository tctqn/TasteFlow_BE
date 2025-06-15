package com.startup.tasteflowbe;

import com.startup.tasteflowbe.config.PayOSConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PayOSConfig.class)
public class TasteFlowBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TasteFlowBeApplication.class, args);
    }

}
