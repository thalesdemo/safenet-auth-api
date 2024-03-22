package com.thalesdemo.safenet.token.api.controller;
// package com.thalesdemo.safenet.token.list.api.controllers;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.thalesdemo.safenet.token.list.api.ApiException;
// import com.thalesdemo.safenet.token.list.api.ConfigService;
// import com.thalesdemo.safenet.token.list.api.TokenDataDTO;
// import com.thalesdemo.safenet.token.list.api.TokenListDTO;
// import com.thalesdemo.safenet.token.list.api.TokenService;

// @RestController
// @RequestMapping("/api/v1")
// public class TokenController {

// @Autowired
// private TokenService tokenService;

// @Autowired
// private ConfigService configService;

// @GetMapping("/getTokens")
// public List<TokenDataDTO> getTokens(
// @RequestParam(required = false) String state,
// @RequestParam(required = false) String type,
// @RequestParam(required = false) String serial,
// @RequestParam(required = false) String container,
// @RequestParam(required = false) String organization,
// @RequestParam(defaultValue = "0") int startRecord,
// @RequestParam(defaultValue = "50") int pageSize) {

// try {
// if (organization == null)
// organization = configService.getOrganization();
// } catch (Exception e) {
// throw new ApiException("Error getting organization.");
// }

// return tokenService.getTokens(state, type, serial, container, organization,
// startRecord, pageSize);
// }

// @GetMapping("/getAllTokens")
// public List<TokenDataDTO> getAllTokens(
// @RequestParam(required = false) String state,
// @RequestParam(required = false) String type,
// @RequestParam(required = false) String serial,
// @RequestParam(required = false) String container,
// @RequestParam(required = false) String organization) {

// try {
// if (organization == null)
// organization = configService.getOrganization();
// } catch (Exception e) {
// throw new ApiException("Error getting organization.");
// }

// return tokenService.getAllTokens(state, type, serial, container,
// organization);
// }

// @GetMapping("/getTokensByOwner")
// public ResponseEntity<?> getTokensByOwner(@RequestParam String userName,
// @RequestParam(required = false) String organization) {
// try {
// TokenListDTO tokenList = tokenService.getTokensByOwner(userName,
// organization, 10000); // assuming 3
// // timeout
// return ResponseEntity.ok(tokenList);
// } catch (Exception e) {
// return
// ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
// }
// }

// @GetMapping("/authenticators/{userName}/list-options")
// public ResponseEntity<?> getOptionsByOwner(@PathVariable String userName,
// @RequestParam(required = false) Optional<String> organization) {
// try {
// List<String> optionsList = tokenService.getOptionsListByOwner(userName,
// organization, 10000); // Assuming 10
// // seconds
// // timeout,
// // adjust as
// // needed
// return ResponseEntity.ok(optionsList);
// } catch (Exception e) {
// return
// ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
// }
// }

// }
