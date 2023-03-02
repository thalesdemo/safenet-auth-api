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
 *
 * The `ResponseCode` class contains server response codes based on the SafeNet authentication service.
 * These codes are used to indicate the result of an authentication attempt, and can be used to determine
 * the appropriate course of action for the client application.
 *
 * The response codes include success, failure, and various challenge codes that require additional steps
 * to be taken in order to authenticate the user.
 *
 * This class provides a set of constants for each possible response code, and includes a utility method
 * for retrieving the `ResponseCode` object associated with a specific code value.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.safenet.auth.api;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(title = "ResponseCode", description = "The server response codes based on the SafeNet authentication service.")
public class ResponseCode {

	 /**
     * Indicates that authentication failed.
     */
	
    @Schema(description = "Response code for authentication failure.")
    public static final ResponseCode AUTH_FAILURE = new ResponseCode(0, "AUTH_FAILURE", "Authentication failed. Please check your credentials and try again.");

    
    /**
     * Indicates that authentication was successful.
     */
    
    @Schema(description = "Response code for authentication success.")
    public static final ResponseCode AUTH_SUCCESS = new ResponseCode(1, "AUTH_SUCCESS", "Authentication successful.");

    
    /**
     * Indicates that a challenge was issued during authentication.
     */
    
    @Schema(description = "Response code for authentication challenge.")
    public static final ResponseCode AUTH_CHALLENGE = new ResponseCode(2, "AUTH_CHALLENGE", "A challenge has been issued during authentication. Please follow the instructions to complete the authentication process.");

    
    /**
     * Indicates that the server PIN was provided.
     */
    
    @Schema(description = "Response code for server PIN provided.")
    public static final ResponseCode SERVER_PIN_PROVIDED = new ResponseCode(3, "SERVER_PIN_PROVIDED", "Server PIN has been provided.");

    
    /**
     * Indicates that the user's PIN needs to be changed.
     */
    
    @Schema(description = "Response code for user PIN change.")
    public static final ResponseCode USER_PIN_CHANGE = new ResponseCode(4, "USER_PIN_CHANGE", "Your PIN needs to be changed.");
    
    
    /**
     * Indicates that outer window authentication is required.
     */
    
    @Schema(description = "Response code for outer window authentication.")
    public static final ResponseCode OUTER_WINDOW_AUTH = new ResponseCode(5, "OUTER_WINDOW_AUTH", "Outer window authentication is required to complete the process.");

    
    /**
     * Indicates that the static password needs to be changed.
     */
    
    @Schema(description = "Response code for changing the static password.")
    public static final ResponseCode CHANGE_STATIC_PASSWORD = new ResponseCode(6, "CHANGE_STATIC_PASSWORD", "Your static password needs to be updated.");
   
    
    /**
     * Indicates that the static password change has failed.
     */
    
    @Schema(description = "Response code for static change failed.")
    public static final ResponseCode STATIC_CHANGE_FAILED = new ResponseCode(7, "STATIC_CHANGE_FAILED", "Failed to change the static password. Please try again later.");

    
    /**
     * Indicates that the PIN change has failed.
     */
    
    @Schema(description = "Response code for PIN change failed.")
    public static final ResponseCode PIN_CHANGE_FAILED = new ResponseCode(8, "PIN_CHANGE_FAILED", "Failed to change the PIN. Please try again later.");

    
    /**
     * Indicates that a fake challenge was issued.
     */
    
    @Schema(description = "Response code for fake challenge.")
    public static final ResponseCode FAKE_CHALLENGE = new ResponseCode(9, "FAKE_CHALLENGE", "Your first factor credentials are invalid. Please enter valid credentials to proceed.");

    
    /**
     * The integer code for this response code.
     */
    
    @JsonView(ResponseCodeViews.WithCode.class)
    private final int code;

    
    /**
     * The name of this response code.
     */
    
    @Schema(description = "The name of the response code.")
    @JsonProperty("name")
    private final String name;

    
    /**
     * A brief description of this response code.
     */
    
    @Schema(description = "A brief description of the response code.")
    @JsonProperty("message")
    private final String message;

    
    /**
     * Constructs a new ResponseCode with the specified code, name, and message.
     *
     * @param code The integer value of the response code.
     * @param name The name of the response code.
     * @param message The message associated with the response code.
     */
    
    @JsonCreator
    public ResponseCode(@JsonProperty("code") int code, @JsonProperty("name") String name, @JsonProperty("message") String message) {
        this.code = code;
        this.name = name;
        this.message = message;
    }

    /**
     * Returns the integer value of the response code.
     *
     * @return The integer value of the response code.
     */
    
    public int getCode() {
        return code;
    }

    /**
     * Returns the message associated with the response code.
     *
     * @return The message associated with the response code.
     */
    
    public String getMessage() {
        return message;
    }
    
    
    /**
     * List of all possible MFA server response codes.
     */
    
    @Schema(description = "List of all possible MFA server response codes.")
    public static final ResponseCode[] ALL_CODES = {
    	    ResponseCode.AUTH_FAILURE,
    	    ResponseCode.AUTH_SUCCESS,
    	    ResponseCode.AUTH_CHALLENGE,
    	    ResponseCode.SERVER_PIN_PROVIDED,
    	    ResponseCode.USER_PIN_CHANGE,
    	    ResponseCode.OUTER_WINDOW_AUTH,
    	    ResponseCode.CHANGE_STATIC_PASSWORD,
    	    ResponseCode.STATIC_CHANGE_FAILED,
    	    ResponseCode.PIN_CHANGE_FAILED,
    	    ResponseCode.FAKE_CHALLENGE
    	};

    
    /**
     * Returns the ResponseCode instance corresponding to the specified HTTP status code.
     *
     * @param code The HTTP status code to search for.
     *
     * @return The ResponseCode instance corresponding to the specified code.
     *
     * @throws IllegalArgumentException if the specified code is not found in the ALL_CODES list.
     */
    
    public static ResponseCode fromCode(int code) {
        // Iterate through all possible ResponseCode instances
        for (ResponseCode responseCode : ALL_CODES) {
            // If the current ResponseCode instance's code matches the specified code, return it
            if (responseCode.getCode() == code) {
                return responseCode;
            }
        }

        // If no ResponseCode instance with the specified code is found, throw an exception
        throw new IllegalArgumentException("Invalid return code: " + code);
    }

    
    /**
     * Returns the name of the response code.
     *
     * @return The name of the response code.
     */
    
    public String getName() {
        return name;
    }
  
}
