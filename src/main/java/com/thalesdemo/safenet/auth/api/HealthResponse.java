package com.thalesdemo.safenet.auth.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonPropertyOrder({ "health", "token_validator", "bsidca_soap_api" })
class HealthResponse {
    private String health;
    private boolean token_validator;
    private boolean bsidca_soap_api;

    // Constructors, getters, setters, etc.
}
