package com.example.springbackend.service.implementation;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.springbackend.dto.NusDtos;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Service
public class NusService {
    
    private final WebClient nusWebClient;
    private static final Logger log = LoggerFactory.getLogger(NusService.class);
    
    @Value("${api.nus.auth}")
    private String authKey;
    
    public NusService(@Qualifier("nusWebClient") WebClient nusWebClient) {
        this.nusWebClient = nusWebClient;
    }
    
  
    public Flux<String> getServiceNamesForStop(String busStopCode) {
        return nusWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/ShuttleService")
                    .queryParam("busstopname", busStopCode)
                    .build())
                .header("Authorization", authKey)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(body -> {
                    try {
                        NusDtos.NusArrivalsResponse response = new ObjectMapper().readValue(body, NusDtos.NusArrivalsResponse.class);
                        return Flux.fromIterable(response.ShuttleServiceResult().shuttles());
                    } catch (Exception e) {
                        log.error("Failed to parse /ShuttleService response for service names", e);
                        return Flux.empty();
                    }
                })
                .map(NusDtos.Shuttle::name)
                .filter(Objects::nonNull)
                .distinct();
    }
}