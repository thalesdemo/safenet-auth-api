package com.thalesdemo.safenet.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import com.thalesdemo.safenet.auth.commons.ResponseCode;
import com.thalesdemo.safenet.token.api.ApiException;
import com.thalesdemo.safenet.token.api.AuthenticatorResponses;
import com.thalesdemo.safenet.token.api.PingService;
import com.thalesdemo.safenet.token.api.TokenService;
import com.thalesdemo.safenet.token.api.AuthenticatorResponses.AuthenticationOption;
import com.thalesdemo.safenet.token.api.AuthenticatorResponses.ErrorResponse;
import com.thalesdemo.safenet.token.api.AuthenticatorResponses.OptionsResponse;

import org.springframework.core.env.Environment;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/authenticate")
@Tag(name = "Authentication", description = "Endpoints related to user authentication options and procedures.")
public class AuthenticatorController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HealthController healthController;

    /**
     * A reference to the BsidcaPingService which provides methods for
     * pinging and checking the connectivity of the Bsidca server.
     * 
     * This field is autowired by Spring for use in this controller. The
     * BsidcaPingService is used to enhance the health check details
     * by pinging the Bsidca server.
     */
    @Autowired
    private PingService bsidcaPingService;

    @Autowired
    private Environment env; // to fetch properties from application.yaml

    /**
     * This logger will be used to log messages in the AuthenticatorController
     * class.
     */

    private static final Logger logger = Logger.getLogger(AuthenticatorController.class.getName());

    @Operation(summary = "Retrieve authentication options by user", description = "Fetch available authentication options for a specific user. Can optionally filter by organization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved options", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticatorResponses.OptionsResponse.class, defaultValue = "{\"options\": [\"push\", \"grid\", \"email\"]}"))),
            @ApiResponse(responseCode = "400", description = "The request contains malformed or invalid data", content = @Content),
            @ApiResponse(responseCode = "401", description = "You have not authenticated to the API using the header X-API-Key.", content = @Content),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred while fetching the user's token options.", content = @Content),
            @ApiResponse(responseCode = "503", description = "The service is currently unavailable.", content = @Content)
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

        // Check the health of BSIDCA
        // TODO: fix this logic, and remove repetition from HealthController
        boolean useGetMethodFromConfig = Boolean
                .parseBoolean(env.getProperty("safenet.bsidca.scheduling.ping-method", "false"));

        boolean bsidcaPingStatus = false;
        try {
            ResponseEntity<Object> pingResponse = bsidcaPingService.handlePing(useGetMethodFromConfig);
            bsidcaPingStatus = (pingResponse.getStatusCodeValue() == 200);
            if (!bsidcaPingStatus) {
                logger.log(Level.WARNING, "Error while pinging BSIDCA: {0}", pingResponse.getBody());
            }
        } catch (ApiException ex) {
            logger.log(Level.WARNING, "Error while pinging BSIDCA: " + ex.getMessage());
        }

        if (!bsidcaPingStatus) {
            // If BSIDCA is not available, return the corresponding error response
            return new ResponseEntity<>(ResponseCode.BSIDCA_SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE);
        }

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
