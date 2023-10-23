// package com.thalesdemo.safenet.token.list.api;

// import java.io.PrintWriter;
// import java.io.StringWriter;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @RequestMapping("/api/v1")
// public class ApiController {

// @Autowired
// private SOAPClientService soapClientService;

// @Autowired
// private ConfigService configService;

// @PostMapping("/connect")
// public ResponseEntity<Object> connect() {
// try {

// return ResponseEntity.ok(soapClientService.connect());

// } catch (Exception ex) {
// System.out.println("TMP STACK TRACE::: in /connect");
// ex.printStackTrace(); // Prints the stack trace to the console
// throw new ApiException("Failed to connect.", ex);

// // // Convert the stack trace to a String and return it as part of the
// response
// // // body
// // StringWriter sw = new StringWriter();
// // PrintWriter pw = new PrintWriter(sw);
// // ex.printStackTrace(pw);
// // String stackTrace = sw.toString();

// // return ResponseEntity.status(500).body(stackTrace);
// }
// }

// @GetMapping("/ping")
// public ResponseEntity<Object> pingGet() {
// return handlePing(true);
// }

// @PostMapping("/ping")
// public ResponseEntity<Object> pingPost() {
// return handlePing(false);
// }

// private ResponseEntity<Object> handlePing(boolean useGet) {
// try {
// // Pass the useGet parameter to pingConnection method.
// return ResponseEntity.ok(soapClientService.pingConnection(useGet));

// } catch (Exception ex) {
// System.out.println("TMP STACK TRACE::: in /ping");
// ex.printStackTrace(); // Prints the stack trace to the console
// throw new ApiException("Failed to ping.", ex);
// }
// }

// @ExceptionHandler(ApiException.class)
// public ResponseEntity<String> handleApiException(ApiException ex) {
// return ResponseEntity.status(500).body(ex.getMessage());
// }

// private static class ApiException extends RuntimeException {
// public ApiException(String message, Throwable cause) {
// super(message, cause);
// }
// }

// // private class SensitiveDataWrapper implements AutoCloseable {
// // private final char[] data;

// // SensitiveDataWrapper(String data) {
// // this.data = data.toCharArray();
// // }

// // @Override
// // public void close() {
// // configService.clearSensitiveData(data);
// // }
// // }
// }

// // @GetMapping("/total-tokens")
// // public ResponseEntity<Object> getTotalTokens(
// // @RequestParam String state,
// // @RequestParam String type,
// // @RequestParam String serial,
// // @RequestParam String container,
// // @RequestParam String organization) {

// // return ResponseEntity.ok(soapClientService.getTotalTokens(state, type,
// // serial, container, organization));
// // }
