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
 * A controller that handles requests related to response codes returned by the SafeNet authentication service.
 *
 * This controller provides an endpoint for retrieving a list of all possible response codes returned by the SafeNet
 * authentication service. The response codes are represented as a list of ResponseCode objects, each of which contains
 * an integer code, a name, and a message.
 *
 * Note that the ResponseCode object also includes a custom response code (code 9) that is not returned by the SafeNet
 * authentication service, but is instead used by this microservice to indicate a false challenge in response to a request.
 * 
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.thalesdemo.safenet.auth.commons.ResponseCode;
import com.thalesdemo.safenet.auth.commons.ResponseCodeViews;
import com.thalesdemo.safenet.auth.commons.ResponseExamples;

import java.util.Arrays;
import java.util.List;

@RestController
@Tag(name = "Authentication")
@RequestMapping("/api/v1")
public class ResponseCodeController {

	/**
	 * Retrieves a list of all possible response codes for the SafeNet
	 * authentication service.
	 *
	 * @return A ResponseEntity object containing the HTTP response, as well as a
	 *         JSON representation of the response code list.
	 *         If the request is successful, the response code is 200.
	 *         If the request fails due to authentication, the response code is 401.
	 *         If the request fails due to an internal server error, the response
	 *         code is 500.
	 *
	 * @implNote This endpoint returns a JSON object that contains all possible
	 *           response codes for the SafeNet authentication service.
	 *           The response codes are returned as an array of JSON objects, each
	 *           of which contains the following fields:
	 *           - code: The numeric code that represents the response code.
	 *           - name: The name of the response code.
	 *           - message: A brief description of the response code.
	 *           Note that the ResponseCode object also includes a custom response
	 *           code (code 9) that is not returned by the SafeNet
	 *           authentication service, but is instead used by this microservice to
	 *           indicate a false challenge to handle a request.
	 *
	 * @see ResponseCode
	 */

	@GetMapping("/authenticate/response-codes")
	@Operation(summary = "Retrieve a list of all response codes available in the SafeNet authentication service", description = "Note that the ResponseCode object also includes a custom response code (code 9) that is not returned by the SafeNet authentication service, but is instead used by this microservice to indicate a false challenge to handle a request.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The request is successful and the list of response codes is returned.", content = @Content(mediaType = "application/json", examples = @ExampleObject(description = "Example of all the status response codes.", value = ResponseExamples.Codes.ALL))),
			@ApiResponse(responseCode = "400", description = "The request is malformed or invalid.", content = @Content),
			@ApiResponse(responseCode = "401", description = "You have not authenticated to the API using the header X-API-Key.", content = @Content),
			@ApiResponse(responseCode = "500", description = "An unexpected error occurred while retrieving the list of response codes.", content = @Content)
	})
	@JsonView(ResponseCodeViews.WithCode.class)
	public ResponseEntity<String> getAllResponseCodes() {

		try {
			// Create object mapper
			ObjectMapper objectMapper = new ObjectMapper();

			// Get all response codes and serialize to JSON
			List<ResponseCode> responseCodes = Arrays.asList(ResponseCode.ALL_CODES);
			String json = objectMapper.writeValueAsString(responseCodes);

			// Return response with content type set to application/json
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<>(json, headers, HttpStatus.OK);
		} catch (JsonProcessingException e) {
			// Return error response if unable to serialize to JSON
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error converting response codes to JSON: " + e.getMessage());
		}
	}

}
