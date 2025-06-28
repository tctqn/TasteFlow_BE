package com.startup.tasteflowbe.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthFilter jwtFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                // ✅ Cho frontend (chỉ cho phép domain thật)
                CorsConfiguration frontendConfig = new CorsConfiguration();
                frontendConfig.addAllowedOrigin("http://localhost:8081"); // Allow your frontend origin
                frontendConfig.addAllowedMethod("*"); // Allow all HTTP methods (GET, POST, etc.)
                frontendConfig.addAllowedHeader("*"); // Allow all headers
                frontendConfig.setAllowCredentials(true);
                source.registerCorsConfiguration("/**", frontendConfig);

                // ✅ Cho webhook (mở all origin vì từ PayOS call)
                CorsConfiguration webhookConfig = new CorsConfiguration();
                webhookConfig.addAllowedOriginPattern("*");
                webhookConfig.addAllowedMethod("*");
                webhookConfig.addAllowedHeader("*");
                webhookConfig.setAllowCredentials(false);
                source.registerCorsConfiguration("/api/webhook/**", webhookConfig);

                return source;
        }


        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .csrf(csrf -> csrf.disable())
                        .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                // Public access
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers(
                                        "/api/products/**",
                                        "/api/stores/**",
                                        "/api/promotions/**",
                                        "/api/vouchers/**",
                                        "/api/payments/**",
                                        "/api/invoices/**",
                                        "/api/categories/**",
                                        "/api/orders/**",
                                        "/api/webhook/**",
                                        "/api/inventories/store/stock")
                                .permitAll()

                                // Đảm bảo test-s3-connection được phép truy cập
                                .requestMatchers("/test-s3-connection",
                                        "/test-bucket-connection",
                                        "/list-bucket-objects",
                                        "/upload-file",
                                        "/download-file").permitAll()

                                // CUSTOMER routes
                                .requestMatchers(
                                        "/api/cart-items/**",
                                      /*  "/api/orders/**",*/
                                        "/api/payments/**",
                                        "/api/refunds/**",
                                        "/api/invoices/**",
                                        "/api/delivery-trackings/**",
                                        "/api/shipping-addresses/**")
                                .hasRole("CUSTOMER")

                                // STAFF + ADMIN routes
                                .requestMatchers(
                                        "/api/inventories/**",
                                        "/api/product-batches/**",
                                        "/api/stock-movements/**",
                                        "/api/order-items/**",
                                        "/api/categories/**",
                                        "/api/products-units/**",
                                        "/api/units/**",
                                       /* "/api/orders/**",*/
                                        "/api/invoices/**",
                                        "/api/payments/**",
                                        "/api/suppliers/**",
                                        "/api/refunds/**",
                                        "/api/warehouses/**",
                                        "/api/delivery-trackings/**")
                                .hasAnyRole("WAREHOUSE_MANAGER", "ADMIN", "STORE_MANAGER", "STORE_STAFF")

                                // ADMIN-only routes
                                .requestMatchers("/api/users/**")
                                .hasRole("ADMIN")

                                // All other requests must be authenticated
                                .anyRequest().authenticated())
                        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                        .formLogin(form -> form.disable())
                        .httpBasic(httpBasic -> httpBasic.disable());
                return http.build();
        }

}
