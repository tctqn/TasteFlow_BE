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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public access
                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers(
//                                "/api/products/**",
//                                "/api/stores/**",
//                                "/api/promotions/**",
//                                "/api/vouchers/**"
//                        ).permitAll()
//
//                        // CUSTOMER routes
//                        .requestMatchers(
//                                "/api/cart-items/**",
//                                "/api/orders/**",
//                                "/api/payments/**",
//                                "/api/refunds/**",
//                                "/api/invoices/**",
//                                "/api/delivery-trackings/**",
//                                "/api/shipping-addresses/**"
//                        ).hasRole("CUSTOMER")
//
//                        // STAFF + ADMIN routes
//                        .requestMatchers(
//                                "/api/inventories/**",
//                                "/api/product-batches/**",
//                                "/api/stock-movements/**",
//                                "/api/order-items/**",
//                                "/api/categories/**",
//                                "/api/products-units/**",
//                                "/api/units/**",
//                                "/api/orders/**",
//                                "/api/invoices/**",
//                                "/api/payments/**",
//                                "/api/refunds/**",
//                                "/api/delivery-trackings/**"
//                        ).hasAnyRole("STAFF", "ADMIN")
//
//                        // ADMIN-only routes
//                        .requestMatchers(
//                                "/api/users/**",
//                                "/api/suppliers/**",
//                                "/api/warehouses/**"
//                        ).hasRole("ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }
}
