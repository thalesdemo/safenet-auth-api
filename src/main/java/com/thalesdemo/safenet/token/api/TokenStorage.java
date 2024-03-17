package com.thalesdemo.safenet.token.api;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalesdemo.safenet.token.api.util.SecurityUtil;

public class TokenStorage {

    private static final String DEFAULT_STORAGE_PATH = "tokens.json";

    private TokenStorage() {
        // Private constructor to prevent instantiation
        throw new IllegalStateException("Utility class");
    }

    public static void storeTokens(List<TokenDataDTO> tokens, String filePath, String encryptionSecretKey)
            throws Exception {
        if (filePath == null || filePath.isEmpty()) {
            filePath = DEFAULT_STORAGE_PATH;
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonOutput = mapper.writeValueAsString(tokens);

        // Encrypt the JSON data
        String encryptedData = SecurityUtil.encrypt(jsonOutput, encryptionSecretKey);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(encryptedData);
        }
    }

}
