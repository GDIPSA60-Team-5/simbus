//package com.example.springbackend.repository;
//
//import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
//import org.springframework.stereotype.Repository;
//
//import com.example.springbackend.model.RouteMongo;
//
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@Repository
//public interface RouteMongoRepository extends ReactiveMongoRepository<RouteMongo, String> {
//    Flux<RouteMongo> findByUserId(String userId);
//    Mono<RouteMongo> findByUserIdAndId(String userId, String id);
//    Mono<Void> deleteByUserIdAndId(String userId, String id);
//}
