package com.fluxflix.mongo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document("users")
public class UserDocument {

    @Id
    private String id;

    private String userId;        // e.g., "u1"
    private String name;          // optional, default null

    private List<String> watchlist = new ArrayList<>();
}
