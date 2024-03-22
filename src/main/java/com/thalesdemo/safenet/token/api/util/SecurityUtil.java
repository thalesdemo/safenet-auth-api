package com.thalesdemo.safenet.token.api.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;

import java.util.Arrays;
import java.util.Base64;

public class SecurityUtil {

    private static final String ENCRYPTION_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int IV_SIZE = 12; // AES-GCM requires a 12-byte IV.

    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String encrypt(String data, String secret) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(secret), "AES");

        // Generating a new IV for each encryption
        byte[] newIV = new byte[IV_SIZE]; // GCM requires a 12-byte IV
        new SecureRandom().nextBytes(newIV);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, newIV);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Base64 encode IV and encrypted data separately, then combine with a colon
        String encodedIV = Base64.getEncoder().encodeToString(newIV);
        String encodedEncryptedData = Base64.getEncoder().encodeToString(encryptedData);

        return encodedIV + ":" + encodedEncryptedData;
    }

    public static char[] decrypt(String combinedData, String secret) throws Exception {
        String[] parts = combinedData.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[1]);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(secret), "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

        return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8).toCharArray();
    }

    /**
     * Clears sensitive data from memory by overwriting it.
     *
     * @param sensitiveData The char array containing sensitive data to be cleared.
     */
    public static void clearSensitiveData(char[] sensitiveData) {
        if (sensitiveData != null) {
            Arrays.fill(sensitiveData, '\0');
        }
    }
}
