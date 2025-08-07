package com.example.springbackend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);

    private final JwtReactiveAuthenticationManager authenticationManager; // inject this

    public JwtAuthenticationWebFilter(JwtReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Authentication auth = new UsernamePasswordAuthenticationToken(token, token);

            return authenticationManager.authenticate(auth)
                    .flatMap(authentication ->
                            chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                    )
                    .onErrorResume(e -> {
                        logger.warn("Authentication failed: {}", e.getMessage());
                        return chain.filter(exchange);
                    });
        }

        return chain.filter(exchange);
    }
}
