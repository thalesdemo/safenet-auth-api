package com.thalesdemo.safenet.server.security;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.NonNull;

import com.thalesdemo.safenet.token.api.util.SecurityUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Initializes the SSL password for the application by decrypting the encrypted keystore password.
 * It also sets the decrypted password into the application's environment properties.
 */
public class WebSslPasswordInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String ERROR_CODE_EMPTY_KEYSTORE_PASSWORD = "ERR002";

    /**
     * Initializes the SSL password for the application context.
     *
     * @param applicationContext the configurable application context
     */
    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        boolean sslEnabled = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        String encryptedKeystorePassword = environment.getProperty("server.ssl.encrypted-key-store-password");
        String encryptionSecretKey = environment.getProperty("encryption.secret-key");

        // Exit if SSL is not enabled
        if (!sslEnabled) {
            return;
        }

        try {
            // Validate that the encrypted keystore password is not empty or null
            if (encryptedKeystorePassword == null || encryptedKeystorePassword.isEmpty()) {
                throw new WebSslPasswordException("Encrypted keystore password is empty or null", ERROR_CODE_EMPTY_KEYSTORE_PASSWORD);
            }

            // Decrypt the keystore password
            char[] decryptedPassword = SecurityUtil.decrypt(encryptedKeystorePassword, encryptionSecretKey);

            // Set the decrypted password into the environment properties
            Map<String, Object> myMap = new HashMap<>();
            myMap.put("server.ssl.key-store-password", new String(decryptedPassword));
            environment.getPropertySources().addFirst(new MapPropertySource("DECRYPTED_PROPS", myMap));

            // Clear sensitive data from memory
            SecurityUtil.clearSensitiveData(decryptedPassword);
        } catch (WebSslPasswordException e) {
            throw new RuntimeException(e.getMessage() + " [Error code: " + e.getErrorCode() + "]");
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt SSL certificate password value", e);
        }
    }

    /**
     * Custom exception class to handle specific cases of empty or null values for keystore password.
     */
    static class WebSslPasswordException extends Exception {
        private final String errorCode;

        /**
         * Constructs a new WebSslPasswordException with the specified detail message and error code.
         *
         * @param message   the detail message
         * @param errorCode the error code
         */
        public WebSslPasswordException(String message, String errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        /**
         * Returns the error code associated with this exception.
         *
         * @return the error code
         */
        public String getErrorCode() {
            return errorCode;
        }
    }
}
