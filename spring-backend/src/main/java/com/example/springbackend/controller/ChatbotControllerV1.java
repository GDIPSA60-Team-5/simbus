package com.example.springbackend.controller;

import com.example.springbackend.dto.BotResponseDTO;
import com.example.springbackend.dto.Coordinates;
import com.example.springbackend.dto.ErrorResponseDTO;
import com.example.springbackend.dto.MessageResponseDTO;
import com.example.springbackend.service.GeocodingService;
import com.example.springbackend.service.DirectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ChatbotControllerV1 {

    private final DirectionService oneMapService;
    private final GeocodingService geocodingService;

    public ChatbotControllerV1(DirectionService oneMapService, GeocodingService geocodingService) {
        this.oneMapService = oneMapService;
        this.geocodingService = geocodingService;
    }

    @GetMapping("/api/chatbot")
    public Mono<BotResponseDTO> handleChatInput(@RequestParam String input) {

        if (input.toLowerCase().contains("hello")) {
            return Mono.just(new MessageResponseDTO("Hello there! How can I help you today?"));
        }

        if (input.toLowerCase().contains("directions")) {
            String startLocation = "Bishan";
            String endLocation = "Orchard";

            Mono<Coordinates> startCoordsMono = geocodingService.getCoordinates(startLocation);
            Mono<Coordinates> endCoordsMono = geocodingService.getCoordinates(endLocation);

            return Mono.zip(startCoordsMono, endCoordsMono)
                    .flatMap(tuple -> {
                        Coordinates start = tuple.getT1();
                        Coordinates end = tuple.getT2();

                        String startCoords = start.latitude() + "," + start.longitude();
                        String endCoords = end.latitude() + "," + end.longitude();

                        return oneMapService.getBusRoutes(startCoords, endCoords)
                                .map(directionsDto -> (BotResponseDTO) directionsDto);
                    })
                    .switchIfEmpty(Mono.just(new ErrorResponseDTO("Sorry, I could not find directions for that request.")));
        }

        return Mono.just(new ErrorResponseDTO("I'm sorry, I don't understand that request."));
    }
}