package com.thalesdemo.safenet.token.list.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.stereotype.Service;

@Service
public class TokenDataService {
    // Define a map to store the mapping of token serials to types
    private Map<String, String> tokenTypeMapping = new HashMap<>();

    private static final Logger logger = Logger.getLogger(TokenDataService.class.getName());

    // Initialize the mapping in the constructor or load it from a file/database
    public TokenDataService() {
        // For example, you can load the mapping from a file or database
        // tokenTypeMapping = loadMappingFromFile();
    }

    // Add a token serial and its corresponding type to the mapping
    public void addTokenType(String serial, String type) {
        tokenTypeMapping.put(serial, type);
    }

    // Get the type of a token based on its serial
    public String getTokenType(String serial) {
        return tokenTypeMapping.get(serial);
    }

    // Define a method to load tokens from the storage file
    public List<TokenDataDTO> loadTokensFromFile(String storageFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File storageFile = new File(storageFilePath);

        if (storageFile.exists() && !storageFile.isDirectory()) {
            // Read the JSON data from the file and convert it to a list of TokenDataDTO
            return objectMapper.readValue(storageFile, new TypeReference<List<TokenDataDTO>>() {
            });
        } else {
            // Handle the case where the file does not exist or is a directory
            return new ArrayList<>(); // Or return any default value as needed
        }
    }

    // You can also implement methods to load and save the mapping from/to a file or
    // database
    // private Map<String, String> loadMappingFromFile() { ... }
    // private void saveMappingToFile() { ... }
}
