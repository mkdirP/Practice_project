package com.example.myjavafx.model;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataStore {
    private static final DataStore instance = new DataStore();
    private JsonNode jsonData;
    private String uploadedFileName;

    public static DataStore getInstance() {
        return instance;
    }
}

