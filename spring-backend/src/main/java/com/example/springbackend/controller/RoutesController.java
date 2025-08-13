//package com.example.springbackend.controller;
//
//
//import com.example.springbackend.model.RouteMongo;
//import com.example.springbackend.repository.RouteMongoRepository;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api")
//public class RoutesController {
//
//    private final RouteMongoRepository routeRepository;
//
//    public RoutesController(RouteMongoRepository routeRepository) {
//        this.routeRepository = routeRepository;
//    }
//
//    @GetMapping("/routes")
//    public Flux<RouteMongo> getSavedRoutes(@AuthenticationPrincipal Jwt jwt) {
//        String userId = jwt.getSubject(); // or jwt.getClaimAsString("userId")
//        return routeRepository.findByUserId(userId);
//    }
//
//    @PostMapping("/sync/route")
//    public Mono<RouteMongo> syncRoute(@AuthenticationPrincipal Jwt jwt,
//                                      @RequestBody Map<String, Object> routeData) {
//        String userId = jwt.getSubject(); // or jwt.getClaimAsString("userId")
//
//        RouteMongo route = new RouteMongo(
//                userId,
//                (String) routeData.get("from"),
//                (String) routeData.get("to"),
//                (String) routeData.get("busStop"),
//                (String) routeData.get("busService"),
//                (String) routeData.get("startTime"),
//                (String) routeData.get("arrivalTime"),
//                (String) routeData.get("notificationNum"),
//                (List<Boolean>) routeData.get("selectedDays")
//        );
//        return routeRepository.save(route);
//    }
//
//    @DeleteMapping("/routes/{routeId}")
//    public Mono<ResponseEntity<Void>> deleteRoute(@AuthenticationPrincipal Jwt jwt,
//                                                  @PathVariable String routeId) {
//        String userId = jwt.getSubject();
//        return routeRepository.findByUserIdAndId(userId, routeId)
//                .flatMap(existing -> routeRepository.delete(existing)
//                        .thenReturn(ResponseEntity.noContent().<Void>build()))
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//
//    @PutMapping("/routes/{routeId}")
//    public Mono<ResponseEntity<RouteMongo>> updateRoute(@AuthenticationPrincipal Jwt jwt,
//                                                        @PathVariable String routeId,
//                                                        @RequestBody Map<String, Object> body) {
//        String userId = jwt.getSubject();
//        return routeRepository.findByUserIdAndId(userId, routeId)
//                .flatMap(existing -> {
//                    if (body.containsKey("from")) existing.setFrom((String) body.get("from"));
//                    if (body.containsKey("to")) existing.setTo((String) body.get("to"));
//                    if (body.containsKey("busStop")) existing.setBusStop((String) body.get("busStop"));
//                    if (body.containsKey("busService")) existing.setBusService((String) body.get("busService"));
//                    if (body.containsKey("startTime")) existing.setStartTime((String) body.get("startTime"));
//                    if (body.containsKey("arrivalTime")) existing.setArrivalTime((String) body.get("arrivalTime"));
//                    if (body.containsKey("notificationNum")) existing.setNotificationNum((String) body.get("notificationNum"));
//                    if (body.containsKey("selectedDays")) {
//                        @SuppressWarnings("unchecked")
//                        var list = (List<Boolean>) body.get("selectedDays");
//                        existing.setSelectedDays(list);
//                    }
//                    existing.setUpdatedAt(LocalDateTime.now());
//                    return routeRepository.save(existing);
//                })
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//}