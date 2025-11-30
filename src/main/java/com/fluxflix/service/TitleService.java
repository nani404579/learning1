package com.fluxflix.service;

import org.springframework.stereotype.Service;

import com.fluxflix.dto.TitleRequest;
import com.fluxflix.dto.TitleResponse;
import com.fluxflix.exception.NotFoundException;
import com.fluxflix.model.TitleEntity;
import com.fluxflix.repository.TitleRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TitleService {

    private final TitleRepository titleRepository;

    // ---------- Convert Entity → Response ----------
    private TitleResponse toResponse(TitleEntity e) {
        TitleResponse r = new TitleResponse();
        r.setShowId(e.getShowId());
        r.setType(e.getType());
        r.setTitle(e.getTitle());
        r.setDirector(e.getDirector());
        r.setCast(e.getCast());
        r.setCountry(e.getCountry());
        r.setDateAdded(e.getDateAdded());
        r.setReleaseYear(e.getReleaseYear());
        r.setRating(e.getRating());
        r.setDuration(e.getDuration());
        r.setListedIn(e.getListedIn());
        r.setDescription(e.getDescription());
        return r;
    }

    // ---------- Get Title by showId ----------
    public Mono<TitleResponse> getByShowId(String showId) {
        return titleRepository.findByShowId(showId)
                .filter(TitleEntity::getIsActive)
                .switchIfEmpty(Mono.error(new RuntimeException("Title not found")))
                .map(this::toResponse);
    }

    // ---------- Create ----------
    public Mono<TitleResponse> createTitle(TitleRequest request) {
        TitleEntity e = new TitleEntity();

        e.setShowId(request.getShowId());
        e.setType(request.getType());
        e.setTitle(request.getTitle());
        e.setDirector(request.getDirector());
        e.setCast(request.getCast());
        e.setCountry(request.getCountry());
        e.setDateAdded(request.getDateAdded());
        e.setReleaseYear(request.getReleaseYear());
        e.setRating(request.getRating());
        e.setDuration(request.getDuration());
        e.setListedIn(request.getListedIn());
        e.setDescription(request.getDescription());
        e.setIsActive(true);

        return titleRepository.save(e)
                .map(this::toResponse);
    }

    // ---------- Update ----------
    public Mono<TitleResponse> updateTitle(String showId, TitleRequest request) {
        return titleRepository.findByShowId(showId)
                .switchIfEmpty(Mono.error(new RuntimeException("Title not found")))
                .flatMap(existing -> {

                    existing.setType(request.getType());
                    existing.setTitle(request.getTitle());
                    existing.setDirector(request.getDirector());
                    existing.setCast(request.getCast());
                    existing.setCountry(request.getCountry());
                    existing.setDateAdded(request.getDateAdded());
                    existing.setReleaseYear(request.getReleaseYear());
                    existing.setRating(request.getRating());
                    existing.setDuration(request.getDuration());
                    existing.setListedIn(request.getListedIn());
                    existing.setDescription(request.getDescription());

                    return titleRepository.save(existing);
                })
                .map(this::toResponse);
    }

    // ---------- Soft delete ----------
    public Mono<Void> softDelete(String showId) {
        return titleRepository.findByShowId(showId)
                .filter(TitleEntity::getIsActive)
                .switchIfEmpty(Mono.error(new NotFoundException("Title not found or already inactive")))
                .flatMap(existing -> {
                    existing.setIsActive(false);
                    return titleRepository.save(existing);
                })
                .then();
    }



    // ---------- Filtering ----------
    public Flux<TitleResponse> filter(Integer year, String rating) {
        return titleRepository.findByYearAndRating(year, rating)
                .map(this::toResponse);
    }
    
    public Flux<TitleResponse> getAll() {
        return titleRepository.findAllActive()
                .map(this::toResponse);
    }

}
