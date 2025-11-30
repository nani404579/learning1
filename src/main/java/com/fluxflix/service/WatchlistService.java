package com.fluxflix.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

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

    // single demo user for assignment
    private static final String USER_ID = "u1";

    // ------------ ADD TO WATCHLIST (Hybrid Transaction) ------------
    public Mono<String> addToWatchlist(String showId) {

        // 1) Validate showId in Postgres (R2DBC)
        return titleRepo.findByShowId(showId)
                .filter(TitleEntity::getIsActive)     // ignore soft-deleted titles
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Title not found or inactive: " + showId)
                ))

                // 2) Load user from Mongo (Reactive)
                .then(userRepo.findByUserId(USER_ID)
                        // if somehow user doesn't exist, create one
                        .switchIfEmpty(Mono.defer(() -> {
                            UserDocument user = new UserDocument();
                            user.setUserId(USER_ID);
                            return userRepo.save(user);
                        }))
                )

                // 3) Add showId in-memory and save back to Mongo
                .flatMap(user -> {
                    if (user.getWatchlist().contains(showId)) {
                        return Mono.just("Already in watchlist");
                    }

                    user.getWatchlist().add(showId);

                    // 4) Single write to Mongo (no write to Postgres → safe hybrid transaction)
                    return userRepo.save(user)
                            .thenReturn("Added to watchlist");
                });
    }


    // ------------ GET WATCHLIST (JOIN Postgres + Mongo) ------------
    public Flux<Map<String, Object>> getWatchlist() {

        return userRepo.findByUserId(USER_ID)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("User not found: " + USER_ID)
                ))

                // user → list of showIds
                .flatMapMany(user -> Flux.fromIterable(user.getWatchlist()))

                // for each showId, fetch title from Postgres and merge
                .flatMap(showId ->
                        titleRepo.findByShowId(showId)
                                .filter(TitleEntity::getIsActive)
                                .map(title -> merge(showId, title))
                );
    }


    // ------------ REMOVE FROM WATCHLIST (optional but nice) ------------
    public Mono<String> removeFromWatchlist(String showId) {

        return userRepo.findByUserId(USER_ID)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("User not found: " + USER_ID)
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


    // helper to build response object
    private Map<String, Object> merge(String showId, TitleEntity t) {
        Map<String, Object> map = new HashMap<>();

        map.put("showId", showId);
        map.put("title", t.getTitle());
        map.put("type", t.getType());
        map.put("director", t.getDirector());
        map.put("rating", t.getRating());
        map.put("country", t.getCountry());

        return map;
    }
}
