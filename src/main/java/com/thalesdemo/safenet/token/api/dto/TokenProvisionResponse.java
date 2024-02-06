package com.thalesdemo.safenet.token.api.dto;

public class TokenProvisionResponse {
    private String message;
    private String additionalInfo; // or any other fields you need

    public TokenProvisionResponse(String message, String additionalInfo) {
        this.message = message;
        this.additionalInfo = additionalInfo;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
