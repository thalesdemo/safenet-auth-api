package com.thalesdemo.safenet.token.list.api;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenStorage {

    private static final String DEFAULT_STORAGE_PATH = "tokens.json";

    public static void storeTokens(List<TokenDataDTO> tokens, String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            filePath = DEFAULT_STORAGE_PATH;
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonOutput = mapper.writeValueAsString(tokens);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(jsonOutput);
        }
    }
}
