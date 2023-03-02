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
package com.thalesdemo.safenet.auth.api;

import java.util.Objects;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication")
public class AuthenticateController {

	/**
	 * This logger will be used to log messages in the AuthenticateController class.
	 */
	
	private static final Logger Log = Logger.getLogger(AuthenticateController.class.getName());

	
	/**
	 * An instance of the Authenticate class for use in this controller.
	 */
	
	private final Authenticate api;


	/**
	 * Constructs a new AuthenticateController instance with the specified Authenticate dependency injected.
	 * 
	 * @param api An instance of the Authenticate class to be used for user authentication.
	 *            This argument is non-null and is assigned to a final field called "api".
	 *            
	 * @throws NullPointerException if the api argument is null.
	 * 
	 * @implNote This constructor is annotated with "@Autowired", which is an implicit way of declaring
	 *           a constructor-based dependency injection. The dependency is injected by the Spring framework
	 *           when the AuthenticateController is constructed.
	 */
	
	public AuthenticateController(Authenticate api) {
	    Objects.requireNonNull(api, "Authenticate dependency cannot be null.");
	    this.api = api;
	}


	/**
	 * Endpoint for authenticating a user with credentials held in the SafeNet authentication server.
	 *
	 * This endpoint accepts a username as a path parameter and an optional AuthenticationRequest object
	 * as a request body. If the request body is empty or the code field in the request body is empty,
	 * this method initiates a new authentication challenge by sending a request to the SafeNet
	 * authentication server. 
	 * 
	 * If the request body contains a non-empty code field, this method validates
	 * the provided code against the challenge sent by the authentication server.
	 *
	 * The result of the authentication is returned as an AuthenticationResponse object. If the
	 * authentication was successful, the response object contains the respective return codes 
	 * that can be used to validate access to the protected endpoint in the client application.
	 
	 * If the authentication was not successful and a challenge is required, the response object 
	 * contains challenge data and information to be displayed to the user. 
	 * 
	 * If the authentication was not successful and no challenge is required, the response object 
	 * contains an error message.
	 * 
	 * @param username The username of the user to authenticate.
	 * @param authenticationRequest The authentication request object used in the request body of the
	 *        `/authenticate` endpoint. The request body is optional for challenge requests.
	 * @return An AuthenticationResponse object containing the result of the authentication.
	 * @throws HttpException if an HTTP error occurs while communicating with the authentication server
	 * @throws ApiException if an error occurs while processing the authentication response
	 **/
	
	@PostMapping("/authenticate/{username}")
	@Operation(summary = "Authenticate a user with credentials held in the SafeNet authentication server",
			   description = "You can trigger a challenge using a blank code or with an empty request body. The state variable is optional unless when responding to a challenge.")
	@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
			   schema = @Schema(implementation = AuthenticationResponse.class),
			   examples = @ExampleObject(description = "Success",
			   value = ResponseExamples.Authentication.AUTH_SUCCESS)),
			   description = "The authentication was successful.")
	@ApiResponse(responseCode = "403",
			   description = "The authentication was denied or challenged.",
			   content = {
				   @Content(mediaType = "application/json",
					        schema = @Schema(implementation = AuthenticationResponse.class),
					        examples = {
	                            @ExampleObject(name = "Authentication denied",
	                            		       description = "The authentication was denied.",
	                            		       value = ResponseExamples.Authentication.AUTH_FAILURE),
	                            @ExampleObject(name = "Authentication challenged",
	                            		       description = "The authentication was challenged.",
	                            		       value = ResponseExamples.Authentication.AUTH_CHALLENGE)
	                        })
			   })
	@ApiResponse(responseCode = "401",
			   content = @Content,
			   description = "You have not authenticated to the API using the header X-API-Key.")
	@ApiResponse(responseCode = "400",
			   content = @Content,
			   description = "The request was invalid or incomplete, possibly due to malformed JSON data.")
	@JsonView(ResponseCodeViews.Standard.class)

	public ResponseEntity<AuthenticationResponse> authenticate(
		    @Parameter(description="The unique identifier of the user") 
		    @PathVariable("username") String username,
		    
		    @Parameter(description="The authentication request object used in the request body of the /authenticate endpoint. " 
		                             +"The request body is optional for challenge requests.")
		    @RequestBody(required=false) 
		    @Schema(example = ResponseExamples.Authentication.AUTH_REQUEST) 
		    AuthenticationRequest authenticationRequest) 
	{

		// Log that a POST request is incoming for the specified username.
		Log.info("Incoming POST /api/v1/authenticate/" + username);

		// If no request body is provided, create an empty AuthenticationRequest object.
		// This handles cases where a challenge needs to be triggered.
		authenticationRequest = authenticationRequest == null ? new AuthenticationRequest(username) : authenticationRequest.setUsername(username);

		// Log that we are processing the request body as an AuthenticationRequest object.
		String message = "Processing request body as: " + authenticationRequest.toString();
		Log.fine(message);

		// Validate the authentication code with the API and get the server's response.
		AuthenticationResponse serverResponse = this.api.validateCode(authenticationRequest);

		// Log the response from the server for debugging purposes.
		Log.info("Responding to authentication request for user: `" + authenticationRequest.getUsername() + "` with: " + serverResponse);

		/*
		 * If the authentication was denied or challenged, return a FORBIDDEN response.
		 * Otherwise, return an OK response.
		 */
		if (serverResponse.isDenied() || serverResponse.isChallenged()) {
		    return new ResponseEntity<>(serverResponse, HttpStatus.FORBIDDEN);
		} else {
		    return new ResponseEntity<>(serverResponse, HttpStatus.OK);
		}

	}
}