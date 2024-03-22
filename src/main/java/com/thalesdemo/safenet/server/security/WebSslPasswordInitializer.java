package com.thalesdemo.safenet.server.security;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.NonNull;

import com.thalesdemo.safenet.token.api.util.SecurityUtil;

import java.util.HashMap;
import java.util.Map;

public class WebSslPasswordInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String encryptedKeystorePassword = environment.getProperty("server.ssl.encrypted-key-store-password");
        String encryptionSecretKey = environment.getProperty("encryption.secret-key");
        try {
            char[] decryptedPassword = SecurityUtil.decrypt(encryptedKeystorePassword, encryptionSecretKey);

            Map<String, Object> myMap = new HashMap<>();
            myMap.put("server.ssl.key-store-password", new String(decryptedPassword));
            environment.getPropertySources().addFirst(new MapPropertySource("DECRYPTED_PROPS", myMap));

            SecurityUtil.clearSensitiveData(decryptedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt SSL certificate password value", e);
        }
    }
}
