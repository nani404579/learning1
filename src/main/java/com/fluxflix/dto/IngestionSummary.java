package com.fluxflix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngestionSummary {
    private long totalRecordsProcessed;
    private long successCount;
    private long failureCount;
}
