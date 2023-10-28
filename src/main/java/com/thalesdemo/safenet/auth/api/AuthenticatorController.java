package com.thalesdemo.safenet.auth.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thalesdemo.safenet.token.list.api.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/authenticate")
@Tag(name = "Authentication", description = "Endpoints related to user authentication options and procedures.")
public class AuthenticatorController {

    @Autowired
    private TokenService tokenService;

    @Operation(summary = "Retrieve authentication options by user", description = "Fetch available authentication options for a specific user. Can optionally filter by organization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved options", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticatorResponses.OptionsResponse.class, defaultValue = "{\"options\": [\"push\", \"grid\", \"email\"]}"))),
            @ApiResponse(responseCode = "400", description = "The request contains malformed or invalid data", content = @Content),
            @ApiResponse(responseCode = "401", description = "You have not authenticated to the API using the header X-API-Key.", content = @Content),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred while fetching the user's token options.", content = @Content)

    // @ApiResponse(responseCode = "500", description = "Internal server error
    // occurred", content = @Content(mediaType = "application/json", schema =
    // @Schema(implementation = AuthenticatorResponses.ErrorResponse.class,
    // defaultValue = "{\"errorMessage\": \"Internal server error occurred\",
    // \"errorCode\": \"INTERNAL_SERVER_ERROR\"}")))
    })

    @GetMapping("/{username}/list-options")
    public ResponseEntity<Object> getOptionsByOwner(
            @Parameter(description = "The unique identifier of the user.", required = true) @PathVariable String username,

            @Parameter(description = "(**Optional**) The organization for which to retrieve authentication options.") @RequestParam(required = false) Optional<String> organization) {

        try {
            // TODO: fix timeout to be configurable from application.properties
            List<AuthenticatorResponses.AuthenticationOption> optionsList = tokenService.getOptionsListByOwner(username,
                    organization, 10000);
            return ResponseEntity.ok(new AuthenticatorResponses.OptionsResponse(optionsList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthenticatorResponses.ErrorResponse(e.getMessage(), "INTERNAL_SERVER_ERROR"));
        }
    }
}
