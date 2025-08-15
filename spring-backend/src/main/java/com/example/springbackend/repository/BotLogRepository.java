package com.example.springbackend.repository;

import com.example.springbackend.dto.ResponseTypeCount;
import com.example.springbackend.model.BotLog;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BotLogRepository extends ReactiveCrudRepository<BotLog, String> {

    // Count all bot logs
    @Query(value = "{}", count = true)
    Mono<Long> countRequest();

    // Count successful bot logs
    @Query(value = "{ 'success': true }", count = true)
    Mono<Long> countSuccessfulResponses();

    // Calculate average response time for successful requests with both requestTime and responseTime
    @Aggregation(pipeline = {
        "{ $match: { 'success': true, 'requestTime': { $exists: true }, 'responseTime': { $exists: true } } }",
        "{ $addFields: { 'responseTimeMs': { $subtract: [ '$responseTime', '$requestTime' ] } } }",
        "{ $group: { '_id': null, 'avgResponseTime': { $avg: '$responseTimeMs' } } }"
    })
    Mono<Double> getAverageResponseTime();

    // Calculate max response time for successful requests
    @Aggregation(pipeline = {
        "{ $match: { 'success': true, 'requestTime': { $exists: true }, 'responseTime': { $exists: true } } }",
        "{ $addFields: { 'responseTimeMs': { $subtract: [ '$responseTime', '$requestTime' ] } } }",
        "{ $group: { '_id': null, 'maxResponseTime': { $max: '$responseTimeMs' } } }"
    })
    Mono<Double> getMaxResponseTime();

    // Calculate min response time for successful requests
    @Aggregation(pipeline = {
        "{ $match: { 'success': true, 'requestTime': { $exists: true }, 'responseTime': { $exists: true } } }",
        "{ $addFields: { 'responseTimeMs': { $subtract: [ '$responseTime', '$requestTime' ] } } }",
        "{ $group: { '_id': null, 'minResponseTime': { $min: '$responseTimeMs' } } }"
    })
    Mono<Double> getMinResponseTime();

    // Get response type distribution
    @Aggregation(pipeline = {
        "{ $match: { 'responseType': { $exists: true, $ne: null } } }",
        "{ $group: { '_id': '$responseType', 'count': { $sum: 1 } } }",
        "{ $sort: { 'count': -1 } }"
    })
    Flux<ResponseTypeCount> getResponseTypeDistribution();

    // Count responses by specific type
    @Query(value = "{ 'responseType': ?0 }", count = true)
    Mono<Long> countByResponseType(String responseType);
}
