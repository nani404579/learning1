package com.fluxflix.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.fluxflix.dto.IngestionSummary;
import com.fluxflix.model.TitleEntity;
import com.fluxflix.service.IngestionService;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminIngestionController {
	
	 private final IngestionService ingestionService;

	    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	    public Mono<IngestionSummary> uploadCsv(@RequestPart("file") Mono<FilePart> filePartMono) {
	        return ingestionService.ingestCsv(filePartMono);
	    }

    // Step 2.3: Receive file only
//    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public Mono<String> uploadCsv(@RequestPart("file") Mono<FilePart> filePartMono) {
//        return filePartMono
//                .map(file -> "Received file: " + file.filename());
//    }
    
//    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public Mono<String> uploadCsv(@RequestPart("file") Mono<FilePart> filePartMono) {
//
//        return filePartMono.flatMap(filePart -> {
//
//            // Temporary location (project directory)
//            String tempFile = "upload_" + System.currentTimeMillis() + ".csv";
//
//            // Save file reactively
//            return filePart.transferTo(java.nio.file.Paths.get(tempFile))
//                    .thenReturn(tempFile);
//        });
//    }
	
	
	
//	@PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public Flux<String> uploadCsv(@RequestPart("file") Mono<FilePart> filePartMono) {
//
//	    return filePartMono.flatMapMany(filePart -> {
//
//	        String tempFile = "upload_" + System.currentTimeMillis() + ".csv";
//
//	        // Save file first
//	        return filePart.transferTo(Paths.get(tempFile))
//	                .thenMany(
//	                        // Step 2.4.2: Read lines as Flux<String>
//	                        Flux.using(
//	                                () -> Files.lines(Paths.get(tempFile)),
//	                                Flux::fromStream,
//	                                stream -> stream.close()
//	                        )
//	                );
//	    });
//	}

	
//	@PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public Flux<String> uploadCsv(@RequestPart("file") Mono<FilePart> filePartMono) {
//
//	    return filePartMono.flatMapMany(filePart -> {
//
//	        String tempFile = "upload_" + System.currentTimeMillis() + ".csv";
//
//	        // Save file first
//	        return filePart.transferTo(Paths.get(tempFile))
//	                .thenMany(
//	                    Flux.using(
//	                        () -> Files.lines(Paths.get(tempFile)),
//	                        Flux::fromStream,
//	                        stream -> stream.close()
//	                    )
//	                    .skip(1)   // Step 2.4.3: Skip header row
//	                );
//
//	    });
//	}
	
	
//	@PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public Flux<String[]> uploadCsv(@RequestPart("file") Mono<FilePart> filePartMono) {
//
//	    return filePartMono.flatMapMany(filePart -> {
//
//	        String tempFile = "upload_" + System.currentTimeMillis() + ".csv";
//
//	        return filePart.transferTo(Paths.get(tempFile))
//	                .thenMany(
//	                        // Read file as Flux<String>
//	                        Flux.using(
//	                                () -> Files.lines(Paths.get(tempFile)),
//	                                Flux::fromStream,
//	                                stream -> stream.close()
//	                        )
//	                        .skip(1)  // remove header
//	                        // Step 2.4.4: Parse CSV safely using OpenCSV
//	                        .map(line -> {
//	                            try {
//	                                CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
//	                                CSVReader reader = new CSVReaderBuilder(new StringReader(line))
//	                                        .withCSVParser(parser)
//	                                        .build();
//	                                return reader.readNext();  // returns String[]
//	                            } catch (Exception e) {
//	                                return null;
//	                            }
//	                        })
//	                );
//	    });
//	}
	
//	@PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public Flux<TitleEntity> uploadCsv(@RequestPart("file") Mono<FilePart> filePartMono) {
//
//	    return filePartMono.flatMapMany(filePart -> {
//
//	        String tempFile = "upload_" + System.currentTimeMillis() + ".csv";
//
//	        return filePart.transferTo(Paths.get(tempFile))
//	            .thenMany(
//	                Flux.defer(() -> {
//	                    try {
//
//	                        CSVParser parser = new CSVParserBuilder()
//	                                .withSeparator(',')
//	                                .withQuoteChar('"')
//	                                .withEscapeChar('\\')
//	                                .withIgnoreQuotations(false)
//	                                .build();
//
//	                        CSVReader reader = new CSVReaderBuilder(
//	                                Files.newBufferedReader(Paths.get(tempFile))
//	                        )
//	                        .withCSVParser(parser)
//	                        .build();
//
//	                        var rows = reader.readAll();
//	                        return Flux.fromIterable(rows);
//
//	                    } catch (Exception e) {
//	                        return Flux.error(e);
//	                    }
//	                })
//	                .skip(1)
//	                .map(row -> {
//	                    System.out.println("ROW = " + Arrays.toString(row));
//	                    return mapToEntity(row);
//	                })
//	            );
//	    });
//	}
//
//	private TitleEntity mapToEntity(String[] c) {
//
//	    TitleEntity e = new TitleEntity();
//
//	    e.setShowId(get(c, 0));
//	    e.setType(get(c, 1));
//	    e.setTitle(get(c, 2));
//	    e.setDirector(get(c, 3));
//	    e.setCast(normalizeList(get(c, 4)));
//	    e.setCountry(get(c, 5));
//	    e.setDateAdded(get(c, 6));
//	    e.setReleaseYear(parseInt(get(c, 7)));
//	    e.setRating(get(c, 8));
//	    e.setDuration(get(c, 9));
//	    e.setListedIn(normalizeList(get(c, 10)));
//	    e.setDescription(get(c, 11));
//
//	    e.setIsActive(true);
//	    return e;
//	}
//
//
//	private String get(String[] arr, int index) {
//	    if (arr == null || index >= arr.length)
//	        return null;
//	    return arr[index] != null ? arr[index].trim() : null;
//	}
//
//	private Integer parseInt(String value) {
//	    try {
//	        return Integer.parseInt(value.trim());
//	    } catch (Exception e) {
//	        return null;
//	    }
//	}
//
//	private String normalizeList(String value) {
//	    if (value == null || value.isEmpty()) {
//	        return null;
//	    }
//
//	    return Arrays.stream(value.split(","))
//	            .map(String::trim)
//	            .filter(s -> !s.isEmpty())
//	            .reduce((a, b) -> a + "," + b)
//	            .orElse(null);
//	}


}
