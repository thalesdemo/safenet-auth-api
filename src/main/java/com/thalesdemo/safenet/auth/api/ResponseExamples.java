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
 * This class provides examples of the response body for various endpoints in the OpenAPI documentation.
 * These examples are intended to illustrate the expected structure and format of the response body.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.auth.api;


public class ResponseExamples {
	
	/**
     * This class contains examples of responses for the authentication endpoints.
	 * It includes examples for a successful authentication, an authentication challenge, and an authentication failure.
	 */
	
	public static class Authentication {
		public static final String AUTH_FAILURE = "{\r\n"
				+ "  \"status\": 0,\r\n"
				+ "  \"response\": {\r\n"
				+ "    \"name\": \"AUTH_FAILURE\",\r\n"
				+ "    \"message\": \"Authentication failed. Please check your credentials and try again.\"\r\n"
				+ "  },\r\n"
				+ "  \"challenge\": {\r\n"
				+ "    \"name\": \"\",\r\n"
				+ "    \"data\": \"\",\r\n"
				+ "    \"state\": \"\"\r\n"
				+ "  },\r\n"
				+ "  \"username\": \"hello@onewelco.me\",\r\n"
				+ "  \"challenged\": false,\r\n"
				+ "  \"denied\": true,\r\n"
				+ "  \"authenticated\": false\r\n"
				+ "}";

	public static final String AUTH_SUCCESS = "{\r\n"
			+ "  \"status\": 1,\r\n"
			+ "  \"response\": {\r\n"
			+ "    \"name\": \"AUTH_SUCCESS\",\r\n"
			+ "    \"message\": \"Authentication successful.\"\r\n"
			+ "  },\r\n"
			+ "  \"challenge\": {\r\n"
			+ "    \"name\": \"password\",\r\n"
			+ "    \"data\": \"\",\r\n"
			+ "    \"state\": \"\"\r\n"
			+ "  },\r\n"
			+ "  \"username\": \"hello@onewelco.me\",\r\n"
			+ "  \"denied\": false,\r\n"
			+ "  \"challenged\": false,\r\n"
			+ "  \"authenticated\": true\r\n"
			+ "}";
	

	public static final String AUTH_CHALLENGE = "{\r\n"
			+ "  \"status\": 2,\r\n"
			+ "  \"response\": {\r\n"
			+ "    \"name\": \"AUTH_CHALLENGE\",\r\n"
			+ "    \"message\": \"A challenge has been issued during authentication. Please follow the instructions to complete the authentication process.\"\r\n"
			+ "  },\r\n"
			+ "  \"challenge\": {\r\n"
			+ "    \"name\": \"GrIDsure\",\r\n"
			+ "    \"data\": \"1111122222333334444455555\",\r\n"
			+ "    \"state\": \"PPPPQQQQRRRR\"\r\n"
			+ "  },\r\n"
			+ "  \"username\": \"hello@onewelco.me\",\r\n"
			+ "  \"denied\": false,\r\n"
			+ "  \"challenged\": true,\r\n"
			+ "  \"authenticated\": false\r\n"
			+ "}";
	

		public static final String AUTH_REQUEST = "{ \"code\": \"123456\", \"state\": \"\"}";

	
		public static final String GridImage = "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";
	}
	
	
	/**
	 * This class provides constants for all possible response codes returned by the SafeNet authentication service.
	 * These codes can be used in response examples and unit tests.
	 * It has been extended to include a custom return code 9 for handling fake challenges (honeypot)
	 */
	
	public static class Codes {
		public static final String ALL = "[\r\n"
				+ "  {\r\n"
				+ "    \"code\": 0,\r\n"
				+ "    \"name\": \"AUTH_FAILURE\",\r\n"
				+ "    \"message\": \"Authentication failed. Please check your credentials and try again.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 1,\r\n"
				+ "    \"name\": \"AUTH_SUCCESS\",\r\n"
				+ "    \"message\": \"Authentication successful.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 2,\r\n"
				+ "    \"name\": \"AUTH_CHALLENGE\",\r\n"
				+ "    \"message\": \"A challenge has been issued during authentication. Please follow the instructions to complete the authentication process.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 3,\r\n"
				+ "    \"name\": \"SERVER_PIN_PROVIDED\",\r\n"
				+ "    \"message\": \"Server PIN has been provided.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 4,\r\n"
				+ "    \"name\": \"USER_PIN_CHANGE\",\r\n"
				+ "    \"message\": \"Your PIN needs to be changed.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 5,\r\n"
				+ "    \"name\": \"OUTER_WINDOW_AUTH\",\r\n"
				+ "    \"message\": \"Outer window authentication is required to complete the process.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 6,\r\n"
				+ "    \"name\": \"CHANGE_STATIC_PASSWORD\",\r\n"
				+ "    \"message\": \"Your static password needs to be updated.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 7,\r\n"
				+ "    \"name\": \"STATIC_CHANGE_FAILED\",\r\n"
				+ "    \"message\": \"The new static password does not meet policy requirements. Please choose a stronger password and try again.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 8,\r\n"
				+ "    \"name\": \"PIN_CHANGE_FAILED\",\r\n"
				+ "    \"message\": \"The provided PIN does not meet policy requirements. Please choose a stronger PIN and try again.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 9,\r\n"
				+ "    \"name\": \"FAKE_CHALLENGE\",\r\n"
				+ "    \"message\": \"Your first factor credentials are invalid. Please enter valid credentials to proceed.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 10,\r\n"
				+ "    \"name\": \"TV_SERVICE_UNAVAILABLE\",\r\n"
				+ "    \"message\": \"The authentication service (TokenValidator) is currently unavailable. Please try again later.\"\r\n"
				+ "  },\r\n"
				+ "  {\r\n"
				+ "    \"code\": 11,\r\n"
				+ "    \"name\": \"BSIDCA_SERVICE_UNAVAILABLE\",\r\n"
				+ "    \"message\": \"The token management (BSIDCA) service is currently unavailable. Please try again later.\"\r\n"
				+ "  }\r\n"
				+ "]";
	}
	
	public static class Health {
		
		public static final String OK = "{\r\n"
				+ "  \"health\": \"ok\",\r\n"
				+ "  \"token_validator\": true\r\n"
				+ "}";
		
		public static final String ERROR = "{\r\n"
				+ "  \"health\": \"error\",\r\n"
				+ "  \"token_validator\": false\r\n"
				+ "}";
	}
}