package com.example.springbackend.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserDetailsAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveUserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public UserDetailsAuthenticationManager(ReactiveUserDetailsServiceImpl userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        return userDetailsService.findByUsername(username)
                .flatMap(userDetails -> {
                    if (passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        return Mono.just(auth);
                    } else {
                        return Mono.error(new BadCredentialsException("Invalid Credentials"));
                    }
                })
                .switchIfEmpty(Mono.error(new BadCredentialsException("User not found")));
    }

}
