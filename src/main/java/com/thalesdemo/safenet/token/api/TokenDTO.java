package com.thalesdemo.safenet.token.api;

import lombok.Data;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON output
@JsonPropertyOrder({ "type", "phone_number", "email", "serial", "state", "operating_system" }) // Defines the serialization order

public class TokenDTO {
    private String type;
    @JsonProperty("phone_number")
    private String phoneNumber; // Optional
    private String email; // Optional
    private List<String> options;
    private String serial;
    private String state = "unknown";
    @JsonProperty("operating_system")
    private String operatingSystem;

    // Lombok @Data generates constructor, getters, setters, equals, hashCode, and
    // toString methods.

    // Custom getter for 'type' to output in lowercase
    @JsonProperty("type")
    public String getType() {
        return type != null ? type.toLowerCase() : null;
    }
}