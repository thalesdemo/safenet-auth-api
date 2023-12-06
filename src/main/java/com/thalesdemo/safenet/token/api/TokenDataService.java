package com.thalesdemo.safenet.token.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalesdemo.safenet.token.api.util.SecurityUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenDataService {
    private Map<String, String> tokenTypeMapping = new HashMap<>();

    private static final Logger logger = Logger.getLogger(TokenDataService.class.getName());

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Value("${safenet.bsidca.token.storage-file}")
    private String storageFilePath;

    public TokenDataService() {
        // Initialize the mapping in the constructor or load it from a file/database
        // tokenTypeMapping = loadMappingFromFile();
    }

    public void addTokenType(String serial, String type) {
        tokenTypeMapping.put(serial, type);
    }

    public String getTokenType(String serial) {
        return tokenTypeMapping.get(serial);
    }

    public List<TokenDataDTO> loadTokensFromFile() throws IOException {
        List<TokenDataDTO> decryptedTokens = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        File storageFile = new File(storageFilePath);

        if (storageFile.exists() && !storageFile.isDirectory()) {
            try (FileInputStream inputStream = new FileInputStream(storageFile)) {
                byte[] encryptedBytes = inputStream.readAllBytes();
                String encryptedData = new String(encryptedBytes);

                String decryptedData = decryptData(encryptedData);
                decryptedTokens = objectMapper.readValue(decryptedData, new TypeReference<List<TokenDataDTO>>() {
                });
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error decrypting and loading tokens from file: " + e.getMessage(), e);
            }
        }

        return decryptedTokens;
    }

    // public void saveTokensToFile(List<TokenDataDTO> tokens) throws Exception {
    // ObjectMapper objectMapper = new ObjectMapper();
    // String jsonData = objectMapper.writeValueAsString(tokens);
    // String encryptedData = encryptData(jsonData);

    // try (FileOutputStream outputStream = new FileOutputStream(storageFilePath)) {
    // outputStream.write(encryptedData.getBytes());
    // logger.info("Stored encrypted tokens to file");
    // logger.info("TEMP!!!!!!!!!!! " + encryptedData);
    // } catch (Exception e) {
    // logger.log(Level.SEVERE, "Error saving tokens to file: " + e.getMessage(),
    // e);
    // }
    // }

    private String encryptData(String data) throws Exception {
        return SecurityUtil.encrypt(data, secretKey);
    }

    private String decryptData(String encryptedData) throws Exception {
        return new String(SecurityUtil.decrypt(encryptedData, secretKey));
    }
}
