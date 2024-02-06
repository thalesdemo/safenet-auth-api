package com.thalesdemo.safenet.token.api;

public enum TokenType {
    // Enum constants
    gridsure("GrIDsure"),
    mobilepass("MobilePASS"),
    sms("SMS"),
    email("SMS");

    // Instance fields
    private final String soapType;

    // Constructor
    TokenType(String soapType) {
        this.soapType = soapType;
    }

    // Getter
    public String getSoapType() {
        return soapType;
    }

    // Static method for case-insensitive matching
    public static TokenType fromString(String text) {
        for (TokenType b : TokenType.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
