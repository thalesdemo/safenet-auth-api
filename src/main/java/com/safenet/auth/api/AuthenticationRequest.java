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
 * The `AuthenticationRequest` class represents an authentication request object used in the request body of the `/authenticate` endpoint.
 * 
 * The object contains information required to authenticate a user, including the user's organization, username, and passcode.
 * 
 * This class is used to create an instance of an authentication request that is sent to the server to authenticate a user.
 * 
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.safenet.auth.api;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthenticationRequest", title = "AuthenticationRequest", description = "The authentication request object used in the request body of the /authenticate endpoint")
public class AuthenticationRequest {

	/**
	 * The username of the user.
	 */
	
	@Hidden
	@Schema(description = "Username", example = "hello@onewelco.me")
	private String username;

	
	/**
	 * The passcode of the user.
	 */
	
	@Schema(description = "Passcode", example = "123456")
	private String code;

	
	/**
	 * Optional: The challenge state variable. Include this field only if the response to a previous
	 * challenge request included a state value. Omit this field if this is the first request in the
	 * authentication flow.
	 */
	
	@Schema(description = "Optional: Challenge state variable", example = "PPPPQQQQRRRR")
	private String state;

	
	/**
	 * The organization name.
	 */
	
	@Hidden
	@Schema(description = "Organization name", example = "Virtual server name")
	private String organization;

	
	/**
	 * Constructs an AuthenticationRequest object with the specified username.
	 *
	 * @param username The username associated with the authentication request.
	 */
	
	@JsonCreator
	public AuthenticationRequest(String username) {
		this.username = username;
	}

	
	/**
	 * Get the passcode associated with this authentication request.
	 * 
	 * @return the passcode
	 */
	
	public String getCode() {
		return code;
	}
	

	/**
	 * Set the passcode associated with this authentication request.
	 * 
	 * @param code the passcode to set
	 * @return this authentication request object
	 */
	
	public AuthenticationRequest setCode(String code) {
		this.code = code;
		return this;
	}

	
	/**
	 * Get the challenge state variable associated with this authentication request.
	 * 
	 * @return the challenge state variable
	 */
	
	public String getState() {
		return state;
	}

	/**
	 * Set the challenge state variable associated with this authentication request.
	 * 
	 * @param state the challenge state variable to set
	 * @return this authentication request object
	 */
	
	public AuthenticationRequest setState(String state) {
		this.state = state;
		return this;
	}
	
	
	/**
	 * Get the organization associated with this authentication request.
	 *
	 * @return the organization
	 */
	
	public String getOrganization() {
	    return organization;
	}

	
	/**
	 * Set the organization associated with this authentication request.
	 *
	 * @param organization the organization to set
	 * @return this authentication request object
	 */
	
	public AuthenticationRequest setOrganization(String organization) {
	    this.organization = organization;
	    return this;
	}

	
	/**
	 * Get the username associated with this authentication request.
	 *
	 * @return the username
	 */
	
	public String getUsername() {
	    return username;
	}

	
	/**
	 * Set the username associated with this authentication request.
	 *
	 * @param username the username to set
	 * @return this authentication request object
	 */
	
	public AuthenticationRequest setUsername(String username) {
		this.username = username;
		return this;
	}
}