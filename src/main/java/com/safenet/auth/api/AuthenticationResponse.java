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
 * AuthenticationResponse represents the response object returned by the /authenticate endpoint.
 * This object contains the authentication result status code, the authentication response code 
 * and an authentication challenge object if authentication requires additional challenge.
 *
 * This object is serialized as a JSON string and returned in the HTTP response body to the client.
 * 
 * @see ResponseCode
 * @see AuthenticationChallenge
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.safenet.auth.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(title="AuthenticationResponse", description = "The response body for the /authenticate endpoint")
public class AuthenticationResponse {

    /**
     * Return status code of the authentication.
     */
	
    @Schema(description = "Return status code of the authentication.")
    private int status;

    
    /**
     * Authentication response.
     */
    
    @Schema(implementation = ResponseCode.class, description = "Authentication response")
    private ResponseCode response;
    
    
    /**
     * Authentication challenge attributes.
     */
    
    @Schema(implementation = AuthenticationChallenge.class, description = "Authentication challenge attributes")
    private AuthenticationChallenge challenge;
    
    
    /**
     * Name of the user from the request.
     */
    
    @Schema(description = "Name of the user from the request.")
    private String username;

    
    /**
     * Creates an instance of AuthenticationResponse with a given username and default challenge.
     *
     * @param username The username of the authenticated user.
     */
    
    public AuthenticationResponse(String username) {
        this.username = username;
        this.challenge = new AuthenticationChallenge();
    }

    
    /**
     * Creates an instance of AuthenticationResponse with a given username and response code.
     *
     * @param username The username of the authenticated user.
     * @param response The response code for the authentication.
     */
    
    public AuthenticationResponse(String username, ResponseCode response) {
        this(username);
        this.setStatus(response);
    }

    
    /**
     * Creates an instance of AuthenticationResponse with a given username, response code, and challenge.
     *
     * @param username The username of the authenticated user.
     * @param response The response code for the authentication.
     * @param challenge The authentication challenge.
     */
    
    public AuthenticationResponse (String username, ResponseCode response, AuthenticationChallenge challenge) {
        this(username, response);
        this.setChallenge(challenge);
    }

	
    /**
     * Indicates whether the authentication was successful.
     *
     * @return true if the authentication was successful, false otherwise
     */
    
    @Schema(description="When set to true, the authentication is successful.")
    public Boolean isAuthenticated() {
        return this.getStatus() == ResponseCode.AUTH_SUCCESS.getCode();
    }
    

    /**
     * Indicates whether the authentication was denied.
     *
     * @return true if the authentication was denied, false otherwise
     */
    
    @Schema(description="When set to true, the authentication is denied.")
    public Boolean isDenied() {
        return this.getStatus() == ResponseCode.AUTH_FAILURE.getCode() 
            || this.getStatus() == ResponseCode.PIN_CHANGE_FAILED.getCode()
            || this.getStatus() == ResponseCode.STATIC_CHANGE_FAILED.getCode();
    }
    
    
    /**
     * Indicates whether the authentication was challenged.
     *
     * @return true if the authentication was challenged, false otherwise
     */
    
    @Schema(description="When set to true, the authentication is challenged.")
    public Boolean isChallenged() {
        return this.getStatus() == ResponseCode.AUTH_CHALLENGE.getCode()
            || this.getStatus() == ResponseCode.USER_PIN_CHANGE.getCode()
            || this.getStatus() == ResponseCode.CHANGE_STATIC_PASSWORD.getCode()
            || this.getStatus() == ResponseCode.OUTER_WINDOW_AUTH.getCode();    
    }


    /**
     * Returns the JSON representation of this AuthenticationResponse object as a string.
     * @return a string representation of the JSON response
     */
 
	@Override
	public String toString() {
		String serverResponseJson = "";
		
		try {
			serverResponseJson = new ObjectMapper().writeValueAsString(this);
			
		} catch (JsonProcessingException e) {
			// TODO: Add logger to the class 
			e.printStackTrace();
		}
		return serverResponseJson;
	}
	
	
	/**
	 * Get the status code of the authentication response.
	 * 
	 * @return the status code
	 */
	
	public int getStatus() {
		return this.getResponse().getCode();
	}

	
	/**
	 * Set the status of the authentication response using a ResponseCode object.
	 * 
	 * @param response the ResponseCode to set based on the ResponseCode object
	 * @return this AuthenticationResponse object
	 */
	
	public AuthenticationResponse setStatus(ResponseCode response) {
		this.status = response.getCode();
		this.setResponse(response);
		return this;
	}

	
	/**
	 * Set the status of the authentication response using an integer code.
	 * 
	 * @param status the integer code to set
	 * @return this AuthenticationResponse object
	 */
	
	public AuthenticationResponse setStatus(int status) {
	    this.status = status;
	    this.setResponse(ResponseCode.fromCode(status));
	    return this;
	}

	
	/**
	 * Get the ResponseCode object associated with the authentication response.
	 * 
	 * @return the ResponseCode object
	 */
	
	public ResponseCode getResponse() {
		return response;
	}

	/**
	 * Set the response code for this authentication response.
	 *
	 * @param response the response code to set
	 * @return this AuthenticationResponse object
	 */
	
	public AuthenticationResponse setResponse(ResponseCode response) {
	    this.response = response;
	    return this;
	}

	
	/**
	 * Get the authentication challenge associated with this response.
	 *
	 * @return the authentication challenge
	 */
	
	public AuthenticationChallenge getChallenge() {
	    return challenge;
	}

	
	/**
	 * Set the authentication challenge associated with this response.
	 *
	 * @param challenge the authentication challenge to set
	 * @return this AuthenticationResponse object
	 */
	
	public AuthenticationResponse setChallenge(AuthenticationChallenge challenge) {
	    this.challenge = challenge;
	    return this;
	}

	
	/**
	 * Returns the username associated with this authentication response.
	 *
	 * @return the username
	 */
	
	public String getUsername() {
	    return username;
	}

	
	/**
	 * Sets the username associated with this authentication response.
	 *
	 * @param username the username to set
	 * @return this authentication response object
	 */
	
	public AuthenticationResponse setUsername(String username) {
	    this.username = username;
	    return this;
	}

}
