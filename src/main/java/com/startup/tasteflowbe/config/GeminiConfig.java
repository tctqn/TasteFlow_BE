package com.startup.tasteflowbe.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public Client geminiClient() {
        String apiKey = System.getenv("GOOGLE_GEMINI_API_KEY");
        return Client.builder().apiKey(apiKey).build();
    }
}
