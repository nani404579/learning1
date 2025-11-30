package com.fluxflix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponse {

    private String showId;

    // from Postgres titles table
    private String title;
    private String type;
    private String rating;
    private String director;
    private String country;
}
