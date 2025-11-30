package com.fluxflix.service;

import org.springframework.stereotype.Service;

import com.fluxflix.dto.WatchlistResponse;
import com.fluxflix.exception.NotFoundException;
import com.fluxflix.model.TitleEntity;
import com.fluxflix.mongo.UserDocument;
import com.fluxflix.mongo.UserRepository;
import com.fluxflix.repository.TitleRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final UserRepository userRepo;
    private final TitleRepository titleRepo;

    // ------------ ADD TO WATCHLIST (Hybrid read SQL + write Mongo) ------------
    public Mono<String> addToWatchlist(String userId, String showId) {

        // 1) Validate in Postgres (read-only, ensures referential integrity)
        return titleRepo.findByShowId(showId)
                .filter(TitleEntity::getIsActive)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Title not found or inactive: " + showId)
                ))

                // 2) Load user from Mongo; if user doesn't exist, create it
                .then(
                    userRepo.findByUserId(userId)
                            .switchIfEmpty(Mono.defer(() -> {
                                UserDocument u = new UserDocument();
                                u.setUserId(userId);
                                return userRepo.save(u);
                            }))
                )

                // 3) Update in-memory and perform a single write to Mongo
                .flatMap(user -> {
                    if (user.getWatchlist().contains(showId)) {
                        return Mono.just("Already in watchlist");
                    }
                    user.getWatchlist().add(showId);
                    return userRepo.save(user)
                                   .thenReturn("Added to watchlist");
                });
    }

    // ------------ GET WATCHLIST (Hybrid JOIN: Mongo + Postgres) ------------
    public Flux<WatchlistResponse> getWatchlist(String userId) {

        return userRepo.findByUserId(userId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("User not found: " + userId)
                ))

                // user → Flux<showId>
                .flatMapMany(user -> Flux.fromIterable(user.getWatchlist()))

                // for each showId → fetch title from Postgres and map to DTO
                .flatMap(showId ->
                        titleRepo.findByShowId(showId)
                                .filter(TitleEntity::getIsActive)
                                .map(title -> toResponse(showId, title))
                );
    }

    private WatchlistResponse toResponse(String showId, TitleEntity t) {
        WatchlistResponse resp = new WatchlistResponse();
        resp.setShowId(showId);
        resp.setTitle(t.getTitle());
        resp.setType(t.getType());
        resp.setRating(t.getRating());
        resp.setDirector(t.getDirector());
        resp.setCountry(t.getCountry());
        return resp;
    }

    // ------------ REMOVE FROM WATCHLIST ------------
    public Mono<String> removeFromWatchlist(String userId, String showId) {

        return userRepo.findByUserId(userId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("User not found: " + userId)
                ))
                .flatMap(user -> {
                    boolean removed = user.getWatchlist().remove(showId);
                    if (!removed) {
                        return Mono.error(
                                new NotFoundException("ShowId not in watchlist: " + showId)
                        );
                    }
                    return userRepo.save(user)
                                   .thenReturn("Removed from watchlist");
                });
    }
}
