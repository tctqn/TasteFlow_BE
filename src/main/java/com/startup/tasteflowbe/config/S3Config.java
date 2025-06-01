package com.startup.tasteflowbe.config;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_SOUTHEAST_1) // Singapore
                .build();
    }
}
