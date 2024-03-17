package com.thalesdemo.safenet.token.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "safenet.authentication-options")
@Data

public class AuthenticationOptions {

    private String defaultPresentationType;
    private Map<String, List<String>> tokenTypeMappings;

    public List<String> getPresentationOptionsForTokenType(String tokenType) {
        List<String> presentationTypes = tokenTypeMappings.get(tokenType);
        if (presentationTypes != null && !presentationTypes.isEmpty()) {
            // Assuming you want the first presentation type if there are multiple
            return presentationTypes;
        }
        return new ArrayList<>(List.of(defaultPresentationType));
    }
}
