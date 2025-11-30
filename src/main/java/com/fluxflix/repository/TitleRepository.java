package com.fluxflix.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.fluxflix.model.TitleEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TitleRepository extends ReactiveCrudRepository<TitleEntity, Long> {

    // Find by unique showId
    Mono<TitleEntity> findByShowId(String showId);

    // Custom query to filter by year and rating (for Milestone 2)
    @Query("SELECT * FROM titles WHERE release_year = :year AND rating = :rating AND is_active = true")
    Flux<TitleEntity> findByYearAndRating(Integer year, String rating);
    
    @Query("SELECT * FROM titles WHERE is_active = true")
    Flux<TitleEntity> findAllActive();
    
}




