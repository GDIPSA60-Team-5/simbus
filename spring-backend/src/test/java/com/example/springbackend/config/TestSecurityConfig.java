package com.example.springbackend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {
    @Bean
    @Primary
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"))
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
