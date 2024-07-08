package com.thalesdemo.safenet.server.security;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.NonNull;

/**
 * Initializer to check the existence of the encryption secret key.
 * This initializer runs before any other initializers to ensure the key is present.
 */
public class EncryptionKeyValidatorInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String ERROR_CODE_EMPTY_SECRET_KEY = "ERR001";

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String encryptionSecretKey = environment.getProperty("encryption.secret-key");

        // Validate that the encryption secret key is not empty or null
        if (encryptionSecretKey == null || encryptionSecretKey.isEmpty()) {
            throw new EncryptionKeyValidatorException("Encryption secret key is empty or null", ERROR_CODE_EMPTY_SECRET_KEY);
        }
    }

    /**
     * Custom exception class to handle the case of an empty or null encryption secret key.
     */
    public static class EncryptionKeyValidatorException extends RuntimeException {
        private final String errorCode;

        /**
         * Constructs a new EncryptionKeyValidatorException with the specified detail message and error code.
         *
         * @param message   the detail message
         * @param errorCode the error code
         */
        public EncryptionKeyValidatorException(String message, String errorCode) {
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
