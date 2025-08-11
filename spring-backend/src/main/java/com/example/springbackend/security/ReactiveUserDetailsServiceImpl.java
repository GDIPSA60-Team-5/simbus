package com.example.springbackend.security;

import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository; // reactive version
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public ReactiveUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUserName(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .map(this::toSpringUser);
    }

    private UserDetails toSpringUser(com.example.springbackend.model.User u) {
        return org.springframework.security.core.userdetails.User.withUsername(u.getUserName())
                .password(u.getPasswordHash())
                .authorities(new String[0])
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

}
