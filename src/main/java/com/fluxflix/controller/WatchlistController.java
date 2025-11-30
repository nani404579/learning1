package com.fluxflix.controller;

import java.util.Map;

import com.fluxflix.service.WatchlistService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService service;

    // POST /api/v1/watchlist/s1
    @PostMapping("/{showId}")
    public Mono<String> addToWatchlist(@PathVariable(name = "showId") String showId) {
        return service.addToWatchlist(showId);
    }

    // GET /api/v1/watchlist
    @GetMapping
    public Flux<Map<String, Object>> getWatchlist() {
        return service.getWatchlist();
    }

    // DELETE /api/v1/watchlist/s1
    @DeleteMapping("/{showId}")
    public Mono<String> removeFromWatchlist(@PathVariable(name = "showId") String showId) {
        return service.removeFromWatchlist(showId);
    }
}
