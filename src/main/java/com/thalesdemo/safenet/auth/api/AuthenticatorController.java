package com.thalesdemo.safenet.auth.api;

import java.util.List;
import java.util.Optional;

import com.thalesdemo.safenet.token.list.api.TokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authenticators")
public class AuthenticatorController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/{userName}/list-options")
    public ResponseEntity<?> getOptionsByOwner(
            @PathVariable String userName,
            @RequestParam(required = false) Optional<String> organization) {
        try {
            List<String> optionsList = tokenService.getOptionsListByOwner(userName, organization, 10000); // Assuming 10
                                                                                                          // seconds
                                                                                                          // timeout,
                                                                                                          // adjust as
                                                                                                          // needed
            return ResponseEntity.ok(optionsList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
