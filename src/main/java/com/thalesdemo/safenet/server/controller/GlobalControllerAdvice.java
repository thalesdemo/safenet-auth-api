package com.thalesdemo.safenet.server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException e) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("status", "error");
        errorDetails.put("message", "404 not found");
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
}