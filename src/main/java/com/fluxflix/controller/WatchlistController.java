package com.fluxflix.controller;

import com.fluxflix.dto.WatchlistAddRequest;
import com.fluxflix.dto.WatchlistResponse;
import com.fluxflix.service.WatchlistService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService service;

    /**
     * POST /api/v1/users/{userId}/watchlist
     * Body: { "showId": "s1" }
     */
    @PostMapping
    public Mono<String> addToWatchlist(
            @PathVariable("userId") String userId,
            @RequestBody WatchlistAddRequest request
    ) {
        return service.addToWatchlist(userId, request.getShowId());
    }

    @GetMapping
    public Flux<WatchlistResponse> getWatchlist(
            @PathVariable("userId") String userId
    ) {
        return service.getWatchlist(userId);
    }

    @DeleteMapping("/{showId}")
    public Mono<String> removeFromWatchlist(
            @PathVariable("userId") String userId,
            @PathVariable("showId") String showId
    ) {
        return service.removeFromWatchlist(userId, showId);
    }

}
