package com.thalesdemo.safenet.token.api.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.AEADBadTagException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalesdemo.safenet.token.api.dto.TokenDataDTO;
import com.thalesdemo.safenet.token.api.util.SecurityUtil;

@Service
public class TokenDataService {
    private Map<String, String> tokenTypeMapping = new HashMap<>();

    private static final Logger logger = Logger.getLogger(TokenDataService.class.getName());

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Value("${safenet.bsidca.token.storage-file}")
    private String storageFilePath;

    public TokenDataService() {
        // TokenDataService is used for storing and retrieving token info
        // from an encrypted file on the server. It is loaded with the
        // ScheduledTasks bean in the application context.
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

            } catch (IllegalArgumentException | AEADBadTagException e) {
                String errorMsg = "Error (" + e.getClass().getSimpleName() + ") while decrypting tokens from file: "
                        + e.getMessage();
                logger.log(Level.SEVERE, errorMsg);
                throw new IOException(errorMsg, e); // Include 'e' as the cause
            } catch (IOException e) {
                String errorMsg = "Error (IOException) reading tokens from file: " + e.getMessage();
                logger.log(Level.SEVERE, errorMsg);
                throw new IOException(errorMsg, e); // Include 'e' as the cause for consistency
            } catch (Exception e) {
                String errorMsg = "Error (Unexpected) while handling tokens from file: " + e.getMessage();
                logger.log(Level.SEVERE, errorMsg, e);
            }
        }

        return decryptedTokens;
    }

    private String encryptData(String data) throws Exception {
        return SecurityUtil.encrypt(data, secretKey);
    }

    private String decryptData(String encryptedData) throws Exception {
        return new String(SecurityUtil.decrypt(encryptedData, secretKey));
    }
}
