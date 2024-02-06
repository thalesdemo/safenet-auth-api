package com.thalesdemo.safenet.token.api.dto;

public class TokenRevokeResponse {
    private String message;
    private String revokeResult;

    public TokenRevokeResponse(String message, String revokeResult) {
        this.message = message;
        this.revokeResult = revokeResult;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRevokeResult() {
        return revokeResult;
    }

    public void setRevokeResult(String revokeResult) {
        this.revokeResult = revokeResult;
    }
}
