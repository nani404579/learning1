package com.fluxflix.controller;

import org.springframework.web.bind.annotation.*;

import com.fluxflix.dto.TitleRequest;
import com.fluxflix.dto.TitleResponse;
import com.fluxflix.service.TitleService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/titles")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;

    // ---------- 1. GET by showId ----------
    @GetMapping("/{showId}")
    public Mono<TitleResponse> getByShowId(@PathVariable(name = "showId") String showId) {
        return titleService.getByShowId(showId);
    }

    // ---------- 2. CREATE ----------
    @PostMapping
    public Mono<TitleResponse> createTitle(@RequestBody TitleRequest request) {
        return titleService.createTitle(request);
    }

    // ---------- 3. UPDATE ----------
    @PutMapping("/{showId}")
    public Mono<TitleResponse> updateTitle(
            @PathVariable(name = "showId") String showId,
            @RequestBody TitleRequest request
    ) {
        return titleService.updateTitle(showId, request);
    }

    // ---------- 4. SOFT DELETE ----------
    @DeleteMapping("/{showId}")
    public Mono<Void> softDelete(@PathVariable(name = "showId") String showId) {
        return titleService.softDelete(showId);
    }

    // ---------- 5. FILTER (year + rating) ----------
    @GetMapping
    public Flux<TitleResponse> filter(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "rating") String rating
    ) {
        return titleService.filter(year, rating);
    }
    
 // GET all active titles
    @GetMapping("/all")
    public Flux<TitleResponse> getAllTitles() {
        return titleService.getAll();
    }

}
