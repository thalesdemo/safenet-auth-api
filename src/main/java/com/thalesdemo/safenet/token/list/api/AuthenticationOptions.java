package com.thalesdemo.safenet.token.list.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Component
@ConfigurationProperties(prefix = "authentication-options")
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
