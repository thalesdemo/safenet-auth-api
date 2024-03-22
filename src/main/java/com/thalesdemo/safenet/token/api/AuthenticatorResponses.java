package com.thalesdemo.safenet.token.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AuthenticatorResponses {

    public enum AuthenticationOption {
        PUSH, GRID, EMAIL, SMS, OTP, VOICE, CODE;

        @JsonValue
        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public static class LowercaseEnumSerializer extends StdSerializer<AuthenticationOption> {
        public LowercaseEnumSerializer() {
            super(AuthenticationOption.class);
        }

        @Override
        public void serialize(AuthenticationOption value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            gen.writeString(value.name().toLowerCase());
        }
    }

    /**
     * Converts a list of AuthenticationOption enums to a list of their string
     * representations.
     *
     * @param optionsList the list of AuthenticationOption enums.
     * @return a List<String> containing the string representations of the enums.
     */
    public static List<String> convertOptionsToStringList(List<AuthenticationOption> optionsList) {
        return optionsList.stream()
                .map(AuthenticationOption::toString) // Utilizes the overridden toString method
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionsResponse {
        private List<AuthenticationOption> options;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorResponse {
        private String errorMessage;
        private String errorCode;
    }

    public static AuthenticationOption fromString(String optionName) {
        for (AuthenticationOption option : AuthenticationOption.values()) {
            if (option.name().equalsIgnoreCase(optionName)) {
                return option;
            }
        }
        throw new IllegalArgumentException("No enum value found for option: " + optionName);
    }

}
