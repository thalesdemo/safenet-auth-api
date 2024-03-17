package com.thalesdemo.safenet.server.controller;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * This class is used to represent the response to a health check request.
 * The response contains the health status of the server and the status of the
 * token validator and the BSIDCA SOAP API.
 */
@Data
@JsonPropertyOrder({ "health", "token_validator", "bsidca_soap_api" })
class HealthResponse {
    private String health;
    private boolean token_validator;
    private boolean bsidca_soap_api;
}
