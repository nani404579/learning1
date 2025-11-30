package com.fluxflix.service;



import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.fluxflix.dto.IngestionSummary;
import com.fluxflix.model.TitleEntity;
import com.fluxflix.repository.TitleRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final TitleRepository titleRepository;

    public Mono<IngestionSummary> ingestCsv(Mono<FilePart> filePartMono) {

        return filePartMono.flatMap(filePart -> {

            Path tempFile = Paths.get("upload_" + System.currentTimeMillis() + "_" + filePart.filename());

            // 1) Save file
            return filePart.transferTo(tempFile)
                    // 2) Parse CSV → Flux<TitleEntity>
                    .thenMany(parseCsv(tempFile))
                    // 3) For each entity: upsert and map to success / failure
                    .flatMap(this::upsertWithResult)
                    // 4) Collect results and build summary
                    .collectList()
                    .map(results -> {
                        long total = results.size();
                        long success = results.stream().filter(Boolean::booleanValue).count();
                        long failure = total - success;
                        return new IngestionSummary(total, success, failure);
                    });
        });
    }

    // ---------- CSV parsing (reactive, on boundedElastic) ----------

    private Flux<TitleEntity> parseCsv(Path tempFile) {

        return Flux.defer(() ->
                Mono.fromCallable(() -> {

                    CSVParser parser = new CSVParserBuilder()
                            .withSeparator(',')
                            .withQuoteChar('"')
                            .withEscapeChar('\\')
                            .withIgnoreQuotations(false)
                            .build();

                    CSVReader reader = new CSVReaderBuilder(
                            Files.newBufferedReader(tempFile)
                    )
                    .withCSVParser(parser)
                    .build();

                    return reader.readAll();            // blocking but offloaded
                })
                .subscribeOn(Schedulers.boundedElastic())
        )
        .flatMap(Flux::fromIterable)
        .skip(1) // skip header
        .map(row -> {
            System.out.println("ROW = " + Arrays.toString(row));
            return mapToEntity(row);
        });
    }

    // ---------- UPSERT logic ----------

    private Mono<Boolean> upsertWithResult(TitleEntity entity) {

        return upsert(entity)
                .thenReturn(true)
                .onErrorResume(ex -> {
                    ex.printStackTrace();   // log error
                    return Mono.just(false);
                });
    }

    private Mono<TitleEntity> upsert(TitleEntity entity) {
        return titleRepository.findByShowId(entity.getShowId())
                .flatMap(existing -> {
                    // update existing
                    existing.setType(entity.getType());
                    existing.setTitle(entity.getTitle());
                    existing.setDirector(entity.getDirector());
                    existing.setCast(entity.getCast());
                    existing.setCountry(entity.getCountry());
                    existing.setDateAdded(entity.getDateAdded());
                    existing.setReleaseYear(entity.getReleaseYear());
                    existing.setRating(entity.getRating());
                    existing.setDuration(entity.getDuration());
                    existing.setListedIn(entity.getListedIn());
                    existing.setDescription(entity.getDescription());
                    existing.setIsActive(true);
                    return titleRepository.save(existing);
                })
                .switchIfEmpty(
                        // insert new
                        titleRepository.save(entity)
                );
    }

    // ---------- Mapping + normalization ----------

    private TitleEntity mapToEntity(String[] c) {

        TitleEntity e = new TitleEntity();

        e.setShowId(get(c, 0));
        e.setType(get(c, 1));
        e.setTitle(get(c, 2));
        e.setDirector(get(c, 3));
        e.setCast(normalizeList(get(c, 4)));      // Step 2.5
        e.setCountry(get(c, 5));
        e.setDateAdded(get(c, 6));
        e.setReleaseYear(parseInt(get(c, 7)));
        e.setRating(get(c, 8));
        e.setDuration(get(c, 9));
        e.setListedIn(normalizeList(get(c, 10))); // Step 2.5
        e.setDescription(get(c, 11));
        e.setIsActive(true);

        return e;
    }

    private String get(String[] arr, int index) {
        if (arr == null || index >= arr.length)
            return null;
        return arr[index] != null ? arr[index].trim() : null;
    }

    private Integer parseInt(String value) {
        try {
            return value != null ? Integer.parseInt(value.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeList(String value) {
        if (value == null || value.isEmpty()) return null;

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }
}

