package com.thalesdemo.safenet.token.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.thalesdemo.safenet.token.api.exception.ApiException;

@Service
public class PingService {

    @Autowired
    private SOAPClientService soapClientService;

    public ResponseEntity<Object> handlePing(boolean useGet) {
        try {
            boolean pingSuccessful = soapClientService.pingConnection(useGet);
            if (!pingSuccessful) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Ping unsuccessful.");
            }
            return ResponseEntity.ok(pingSuccessful);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ApiException("Failed to ping. " + ex.getMessage());
        }
    }

}
