package com.thalesdemo.safenet.server.controller;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException{

    // This class is used to represent a 404 Not Found error. It is thrown when a
    // request is made for a resource that does not exist.
    public ResourceNotFoundException() {
      // empty
    }

}
