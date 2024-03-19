package com.thalesdemo.safenet.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thalesdemo.safenet.auth.commons.ResponseCode;
import com.thalesdemo.safenet.token.api.ApiException;
import com.thalesdemo.safenet.token.api.AuthenticatorResponses;
import com.thalesdemo.safenet.token.api.PingService;
import com.thalesdemo.safenet.token.api.TokenDTO;
import com.thalesdemo.safenet.token.api.TokenListDTO;
import com.thalesdemo.safenet.token.api.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/authenticate")
@Tag(name = "Authentication")
public class AuthenticatorController {

    private TokenService tokenService;

    /**
     * A reference to the BsidcaPingService which provides methods for
     * pinging and checking the connectivity of the Bsidca server.
     * 
     * This field is autowired by Spring for use in this controller. The
     * BsidcaPingService is used to enhance the health check details
     * by pinging the Bsidca server.
     */
    private PingService bsidcaPingService;

    private Environment env; // to fetch properties from application.yaml

    /**
     * This logger will be used to log messages in the AuthenticatorController
     * class.
     */

    private static final Logger logger = Logger.getLogger(AuthenticatorController.class.getName());

    public AuthenticatorController(TokenService tokenService, PingService bsidcaPingService, Environment env) {
        this.tokenService = tokenService;
        this.bsidcaPingService = bsidcaPingService;
        this.env = env;
    }

    private static final String tokenListExample = """
            [
               {
                 "type": "options",
                 "options": [
                   "sms",
                   "email",
                   "voice",
                   "grid",
                   "push",
                   "code"
                 ],
                 "remaining_attempts": 2,
                 "num_total_failures": 1,
                 "max_attempt_policy": 3
               },
               {
                   "type": "email",
                   "email_address": "hello@onewelco.me",
                   "serial_number": "1000000000",
                   "last_auth_success": "2024-03-02T00:00:01.00-07:00",
                   "failed_attempts": 1,
                   "state": "active"
               },
               {
                   "type": "mobilepass",
                   "serial_number": "1000000001",
                   "operating_system": "macOS",
                   "push_otp_capable": true,
                   "last_auth_success": "2024-03-01T19:00:00.00-07:00",
                   "failed_attempts": 0,
                   "state": "suspended"
               },
               {
                 "type": "sms",
                 "phone_number": "+1-111-222-3333",
                 "serial_number": "1000000000",
                 "last_auth_success": "2024-03-02T00:00:01.00-07:00",
                 "failed_attempts": 1,
                 "state": "active"
               },
               {
                 "type": "voice",
                 "phone_number": "+1-111-222-3333",
                 "serial_number": "1000000000",
                 "last_auth_success": "2024-03-02T00:00:01.00-07:00",
                 "failed_attempts": 1,
                 "state": "active"
               }
            ]
            """;

    @Operation(summary = "Retrieve authentication options by user", description = "Fetch available authentication options for a specific user. Can optionally filter by organization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved options", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TokenDTO.class)), examples = @ExampleObject(description = "Success", value = tokenListExample))),
            @ApiResponse(responseCode = "400", description = "The request was invalid or incomplete, possibly due to incomplete or malformed data.", content = @Content(schema = @Schema(type = "object", additionalProperties = Schema.AdditionalPropertiesValue.FALSE))),
            @ApiResponse(responseCode = "401", description = "You have not authenticated to the API using the header X-API-Key.", content = @Content),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred while fetching the user's token options.", content = @Content),
            @ApiResponse(responseCode = "503", description = "The service is currently unavailable.", content = @Content)
    })

    // @ApiResponse(responseCode = "500", description = "Internal server error
    // occurred", content = @Content(mediaType = "application/json", schema =
    // @Schema(implementation = AuthenticatorResponses.ErrorResponse.class,
    // defaultValue = "{\"errorMessage\": \"Internal server error occurred\",
    // \"errorCode\": \"INTERNAL_SERVER_ERROR\"}")))

    @GetMapping("/{username}/list-options")
    public ResponseEntity<?> getOptionsByOwner(
            @Parameter(description = "The unique identifier of the user.", required = true) @PathVariable String username,

            @Parameter(description = "(**Optional**) The organization for which to retrieve authentication options.") @RequestParam(required = false) Optional<String> organization,

            @Parameter(description = "Set to `true` for a compact response. Defaults to `false`.", schema = @Schema(type = "string", allowableValues = {
                    "true",
                    "false" })) @RequestParam(name = "compactResponse", defaultValue = "false") boolean compactResponse) {

        // Check the health of BSIDCA
        // TODO: fix this logic, and remove repetition from HealthController
        boolean useGetMethodFromConfig = Boolean
                .parseBoolean(env.getProperty("safenet.bsidca.scheduling.ping-method", "false"));

        boolean bsidcaPingStatus = false;
        try {
            ResponseEntity<Object> pingResponse = bsidcaPingService.handlePing(useGetMethodFromConfig);
            bsidcaPingStatus = (pingResponse.getStatusCode().value() == 200);
            if (!bsidcaPingStatus) {
                logger.log(Level.WARNING, "Error while pinging BSIDCA: {0}", pingResponse.getBody());
            }
        } catch (ApiException ex) {
            logger.log(Level.WARNING, "Error while pinging BSIDCA: {0}", ex.getMessage());
        }

        if (!bsidcaPingStatus) {
            // If BSIDCA is not available, return the corresponding error response
            return new ResponseEntity<>(ResponseCode.BSIDCA_SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            // TODO: fix timeout to be configurable from application.properties
            if (compactResponse) {
                List<AuthenticatorResponses.AuthenticationOption> optionsList = tokenService
                        .getOptionsListByOwner(username, organization, 10000);
                List<TokenDTO> tokens = new ArrayList<>();
                TokenDTO token = new TokenDTO();
                token.setOptions(AuthenticatorResponses.convertOptionsToStringList(optionsList));
                tokens.add(token);
                return ResponseEntity.ok(tokens);
            } else {
                TokenListDTO tokenList = tokenService.getTokensByOwner(username, organization, 10000);
                List<TokenDTO> tokens = tokenList.getTokens();
                return ResponseEntity.ok(tokens);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthenticatorResponses.ErrorResponse(e.getMessage(), "INTERNAL_SERVER_ERROR"));
        }
    }
}
