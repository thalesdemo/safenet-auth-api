/**
 * Copyright 2023 safenet-auth-api
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The AuthenticateController class is responsible for handling authentication-related requests.
 * This includes handling requests for authenticating a user with credentials held in the SafeNet authentication server.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.server.controller;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.thalesdemo.safenet.auth.api.Authenticate;
import com.thalesdemo.safenet.auth.api.CustomAuthenticate;
import com.thalesdemo.safenet.auth.commons.AuthenticationRequest;
import com.thalesdemo.safenet.auth.commons.AuthenticationResponse;
import com.thalesdemo.safenet.auth.commons.ResponseCode;
import com.thalesdemo.safenet.auth.commons.ResponseCodeViews;
import com.thalesdemo.safenet.auth.commons.ResponseExamples;
import com.thalesdemo.safenet.token.api.exception.ApiException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication")
public class AuthenticateController {

	/**
	 * This logger will be used to log messages in the AuthenticateController class.
	 */

	private static final Logger Log = Logger.getLogger(AuthenticateController.class.getName());

	/**
	 * An instance of the HealthController class for use in this controller.
	 */
	// @Autowired
	// private HealthController healthController;

	/**
	 * An instance of the Authenticate class for use in this controller.
	 */

	private final Authenticate api;

	/**
	 * An instance of the CustomAuthenticate class for use in this controller.
	 */

	@Autowired
	private CustomAuthenticate customApi;

	/**
	 * An instance of the HttpServletRequest class for use in this controller.
	 */

	@Autowired
	private HttpServletRequest request;

	/**
	 * Constructs a new AuthenticateController instance with the specified
	 * Authenticate dependency injected.
	 * 
	 * @param api An instance of the Authenticate class to be used for user
	 *            authentication.
	 *            This argument is non-null and is assigned to a final field called
	 *            "api".
	 * 
	 * @throws NullPointerException if the api argument is null.
	 * 
	 * @implNote This constructor is annotated with "@Autowired", which is an
	 *           implicit way of declaring
	 *           a constructor-based dependency injection. The dependency is
	 *           injected by the Spring framework
	 *           when the AuthenticateController is constructed.
	 */

	public AuthenticateController(Authenticate api) {
		Objects.requireNonNull(api, "Authenticate dependency cannot be null.");
		this.api = api;
	}

	/**
	 * Endpoint for authenticating a user with credentials held in the SafeNet
	 * authentication server.
	 *
	 * This endpoint accepts a username as a path parameter and an optional
	 * AuthenticationRequest object
	 * as a request body. If the request body is empty or the code field in the
	 * request body is empty,
	 * this method initiates a new authentication challenge by sending a request to
	 * the SafeNet
	 * authentication server.
	 * 
	 * If the request body contains a non-empty code field, this method validates
	 * the provided code against the challenge sent by the authentication server.
	 *
	 * The result of the authentication is returned as an AuthenticationResponse
	 * object. If the
	 * authentication was successful, the response object contains the respective
	 * return codes
	 * that can be used to validate access to the protected endpoint in the client
	 * application.
	 * 
	 * If the authentication was not successful and a challenge is required, the
	 * response object
	 * contains challenge data and information to be displayed to the user.
	 * 
	 * If the authentication was not successful and no challenge is required, the
	 * response object
	 * contains an error message.
	 * 
	 * @param username              The username of the user to authenticate.
	 * @param authenticationRequest The authentication request object used in the
	 *                              request body of the
	 *                              `/authenticate` endpoint. The request body is
	 *                              optional for challenge requests.
	 * @return An AuthenticationResponse object containing the result of the
	 *         authentication.
	 * @throws HttpException if an HTTP error occurs while communicating with the
	 *                       authentication server
	 * @throws ApiException  if an error occurs while processing the authentication
	 *                       response
	 **/

	@PostMapping("/authenticate/{username}")
	@Operation(summary = "Authenticate a user with credentials held in the SafeNet authentication server", description = "You can trigger a challenge using a blank code or with an empty request body, to the exception of push authentication. The state variable is optional in most cases, however it is mandatory when responding to a challenge. \n\nPush OTP authentication can be initiated using the special code `p` in the request body or using the query parameter `push_mode`, as described below.")
	@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class), examples = @ExampleObject(description = "Success", value = ResponseExamples.Authentication.AUTH_SUCCESS)), description = "The authentication was successful.")
	@ApiResponse(responseCode = "403", description = "The authentication was denied or challenged.", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class), examples = {
					@ExampleObject(name = "Authentication denied", description = "The authentication was denied.", value = ResponseExamples.Authentication.AUTH_FAILURE),
					@ExampleObject(name = "Authentication challenged", description = "The authentication was challenged.", value = ResponseExamples.Authentication.AUTH_CHALLENGE)
			})
	})
	@ApiResponse(responseCode = "401", content = @Content, description = "You have not authenticated to the API using the header X-API-Key.")
	@ApiResponse(responseCode = "400", description = "The request was invalid or incomplete, possibly due to malformed JSON data.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))) // AdditionalPropertiesValue.FALSE)))
	@ApiResponse(responseCode = "503", description = "The service is currently unavailable.", content = @Content)

	@JsonView(ResponseCodeViews.Standard.class)

	public ResponseEntity<AuthenticationResponse> authenticate(
			@Parameter(description = "The unique identifier of the user") @PathVariable("username") String username,

			@Parameter(description = "(**Optional**) The push mode setting; this parameter only applies to push authentication.<br/><br/>**Usage**"
					+ "<ul><li>when set to `one-step` or `quicklog` or when parameter `unset`— the push authentication request will be handled in a single step (a.k.a. quicklog)<br/>"
					+ "→ the `state` parameter is not applicable with this push mode</li><br/>"
					+ "<li>when set to `challenge-response`— the push authentication request will be handled in two steps, offering richer feedback in the user interface, by signaling if and when the push challenge was generated and pending user push approval<br/>"
					+ "→ by setting this option to `challenge-response`, the client application must also set the `state` parameter in the request body of the second request</li></ul><br/>"
					+ "**Specifications**<ul>"
					+ "<li>push authentication takes precedence over any OTP authentication method specified in the request body when the query parameter `push_mode` is set</li><br/>"
					+ "<li>in the `challenge-response` mode, it is recommended to make second `/authenticate` request immediately after completing the first request</li><br/>"
					+ "<li>the second request will be *parked* in the SafeNet Cloud parking server (e.g., sps.us.safenetid.com) for up to **120** seconds or **10** seconds for network connection timeouts</li><br/>"
					+ "<li>push authentication can alternately be initiated by only sending the `code` parameter set to the single character `p` (<b>not</b> case-sensitive) in the request body<br/>"
					+ "→ the default value is `one-step` when push authentication is made without this query parameter but using this special `code` in the request body</li></ul>\n\n", schema = @Schema(type = "string", allowableValues = {
							"one-step (quicklog)",
							"challenge-response" })) @RequestParam(value = "push_mode", required = false) String pushMode,

			@Parameter(description = "The authentication request object used in the request body of the /authenticate endpoint. "
					+ "The request body is optional for challenge requests.") @RequestBody(required = false) @Schema(example = ResponseExamples.Authentication.AUTH_REQUEST) AuthenticationRequest authenticationRequest) {

		// Log that a POST request is incoming for the specified username.
		Log.log(Level.INFO, "Incoming POST /api/v1/authenticate/{0}", username);

		// Check headers for the client IP address.
		String ipAddress = request.getHeader("X-Forwarded-For");
		String unknownString = "unknown";
		if (ipAddress == null || ipAddress.isEmpty() || unknownString.equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || unknownString.equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || unknownString.equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || unknownString.equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ipAddress == null || ipAddress.isEmpty() || unknownString.equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
		}

		// Convert IPv6 address to IPv4 if necessary
		if (ipAddress.contains(":")) {
			int index = ipAddress.lastIndexOf(":");
			ipAddress = ipAddress.substring(index + 1);
		}

		// Log the client IP address in debug mode.
		Log.log(Level.FINE, "Client IP Address: {0}", ipAddress);

		// Check the health of the SafeNet authentication server and return
		// SERVICE_UNAVAILABLE if TokenValidator is down.
		if (!this.api.getServerStatus()) {
			AuthenticationResponse authResponse = new AuthenticationResponse(null, ResponseCode.TV_SERVICE_UNAVAILABLE,
					null);
			return new ResponseEntity<>(authResponse, HttpStatus.SERVICE_UNAVAILABLE);
		} else {
			Log.info("Health check OK. Continuing with authentication request.");
		}

		// If no request body is provided, create an empty AuthenticationRequest object.
		// This handles cases where a challenge needs to be triggered.
		authenticationRequest = authenticationRequest == null ? new AuthenticationRequest(username)
				: authenticationRequest.setUsername(username);

		// Log that we are processing the request body as an AuthenticationRequest
		// object.
		String message = "Processing request body as: " + authenticationRequest.toString();
		Log.fine(message);

		// If the request is a push authentication request, handle it differently. Push
		// is not supported by the official SafeNet Java API.
		// Push is triggered by sending the code "p" (or "P") in the request body or by
		// setting the push_mode query parameter.
		AuthenticationResponse serverResponse = null;
		if (pushMode != null || "p".equalsIgnoreCase(authenticationRequest.getCode())) {
			Log.info("Push OTP authentication request detected for user: " + authenticationRequest.getUsername());
			serverResponse = this.customApi.pushOTP(authenticationRequest.getUsername(), ipAddress,
					authenticationRequest.getState(), pushMode);
		} else {
			// Validate the authentication code with the official Java API and get the
			// server's response.
			serverResponse = this.api.validateCode(authenticationRequest);
		}

		// Log the response from the server for debugging purposes.
		String authResponseMessage = "Responding to authentication request for user: `"
				+ authenticationRequest.getUsername() + "` with: " + serverResponse;
		Log.info(authResponseMessage);

		/*
		 * If the authentication was authenticated, return an OK response.
		 * Otherwise, return a FORBIDDEN response.
		 */
		boolean isAuthenticated = serverResponse.isAuthenticated();
		if (isAuthenticated) {
			return new ResponseEntity<>(serverResponse, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(serverResponse, HttpStatus.FORBIDDEN);
		}

	}
}