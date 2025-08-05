package com.example.springbackend.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import org.springframework.context.annotation.Primary;

@Primary
@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider tokenProvider;
    private final ReactiveUserDetailsServiceImpl userDetailsService;

    public JwtReactiveAuthenticationManager(JwtTokenProvider tokenProvider,
                                            ReactiveUserDetailsServiceImpl userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        if (token == null || !tokenProvider.validateToken(token)) {
            return Mono.empty();
        }
        String username = tokenProvider.getUsernameFromToken(token);
        return userDetailsService.findByUsername(username)
                .flatMap(userDetails -> {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    return Mono.just(auth);
                });
    }
}
