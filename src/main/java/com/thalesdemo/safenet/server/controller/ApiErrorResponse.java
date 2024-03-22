package com.thalesdemo.safenet.server.controller;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class ApiErrorResponse {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
}
