package com.thalesdemo.safenet.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import com.thalesdemo.safenet.token.api.TokenService;
import com.thalesdemo.safenet.token.api.TokenType;
import com.thalesdemo.safenet.token.api.dto.TokenProvisionResponse;
import com.thalesdemo.safenet.token.api.dto.TokenRevokeResponse;
//import com.thalesdemo.safenet.token.api.responses.ManagementResponses;
import com.thalesdemo.safenet.token.api.ConfigService;
import com.thalesdemo.safenet.token.api.TokenListDTO;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/management")
@Tag(name = "Management", description = "Endpoints for token management and administrative operations.")
public class ManagementController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ConfigService configService;

    @Operation(summary = "Revoke a user token", description = "Revokes a token associated with a specific user. The token serial and username are required. The organization parameter is optional; if not provided, a default value from the configuration service is used.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token successfully revoked", content = @Content(schema = @Schema(implementation = TokenRevokeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content)
    })
    @DeleteMapping("/user/{username}/token/{serial}")
    public ResponseEntity<TokenRevokeResponse> revokeToken(
            @Parameter(description = "Username associated with the token.", required = true) @PathVariable String username,

            @Parameter(description = "Serial number of the token to be revoked.", required = true) @PathVariable String serial,

            @Parameter(description = "Organization to which the token belongs. If not provided, a default value is used.", required = false) @RequestParam Optional<String> organization) {

        try {
            String org = organization.orElseGet(() -> {
                try {
                    return configService.getOrganization();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to retrieve default organization", e);
                }
            });

            // Assume 'revokeMode' and 'revokeStaticPassword' are determined within this
            // method or another service
            TokenRevokeResponse response = tokenService.revokeToken(serial, username, "Auto-generated comment",
                    "ReturntoInventory_Initialized", false, org, 10000);
            // return ResponseEntity.ok(new
            // ManagementResponses.RevokeTokenResponse(response));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    // .body(new ManagementResponses.ErrorResponse(e.getMessage(),
                    // "INTERNAL_SERVER_ERROR"));
                    .body(new TokenRevokeResponse("Error: " + e.getMessage(), "INTERNAL_SERVER_ERROR"));
        }
    }

    @Operation(summary = "Delete all tokens for a user", description = "Deletes all tokens associated with a specific user. This operation should be used with caution.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All tokens for the user successfully deleted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content)
    })
    @DeleteMapping("/user/{username}/tokens")
    public ResponseEntity<Object> deleteAllUserTokens(
            @Parameter(description = "Username associated with the tokens.", required = true) @PathVariable String username,
            @Parameter(description = "Organization to which the user belongs. If not provided, a default value is used.", required = false) @RequestParam Optional<String> organization) {

        try {
            String org = organization.orElseGet(() -> {
                try {
                    return configService.getOrganization();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to retrieve default organization", e);
                }
            });

            // Retrieve all tokens for the given user
            TokenListDTO tokenList = tokenService.getTokensByOwner(username, org, 10000);
            List<String> tokenSerials = tokenList.getSerials();

            // Iterate over each token and revoke it
            for (String serial : tokenSerials) {
                tokenService.revokeToken(serial, username, "Batch deletion", "ReturntoInventory_Initialized", false,
                        org, 10000);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    // .body(new ManagementResponses.ErrorResponse(e.getMessage(),
                    // "INTERNAL_SERVER_ERROR"));
                    .body("INTERNAL_SERVER_ERROR: " + e.getMessage());
        }
    }

    @Operation(summary = "Get tokens by user", description = "Retrieves a list of tokens associated with a specific user. The organization parameter is optional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tokens successfully retrieved", content = @Content(schema = @Schema(implementation = TokenListDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content)
    })
    @GetMapping("/user/{username}/tokens")
    public ResponseEntity<Object> getTokensByOwner(
            @Parameter(description = "Username associated with the tokens.", required = true) @PathVariable String username,

            @Parameter(description = "Organization to which the user belong. If not provided, a default value is used.", required = false) @RequestParam Optional<String> organization) {

        try {
            // Assuming a default timeout value, which can be made configurable
            int timeout = 10000;

            String org = organization.orElseGet(() -> {
                try {
                    return configService.getOrganization();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to retrieve default organization", e);
                }
            });

            // Call the TokenService to retrieve tokens for the user
            TokenListDTO tokenList = tokenService.getTokensByOwner(username, org, timeout);
            return ResponseEntity.ok(tokenList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("INTERNAL_SERVER_ERROR: " + e.getMessage());
            // .body(new ManagementResponses.ErrorResponse(e.getMessage(),
            // "INTERNAL_SERVER_ERROR"));
        }
    }

    @Operation(summary = "Enroll token to a user", description = " with a specific user. The organization parameter is optional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tokens successfully retrieved", content = @Content(schema = @Schema(implementation = TokenProvisionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content)
    })
    @PostMapping("/user/{username}/enroll/{token_type}")
    public ResponseEntity<TokenProvisionResponse> provisionTokenToUser(
            @Parameter(description = "Username associated with the tokens.", required = true) @PathVariable String username,

            @Parameter(description = "Token type to enroll to the user.", required = true) @PathVariable("token_type") TokenType tokenType,

            @Parameter(description = "Organization to which the user belongs. If not provided, a default value is used.", required = false) @RequestParam Optional<String> organization) {

        try {
            String org = organization.orElseGet(() -> {
                try {
                    return configService.getOrganization();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to retrieve default organization", e);
                }
            });

            // Use the enum's soapType for the SOAP call
            String soapType = tokenType.getSoapType();

            // Call the TokenService to provision the token for the user
            TokenProvisionResponse response = tokenService.provisionToken(username, soapType, org);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            TokenProvisionResponse response = new TokenProvisionResponse("Error: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
