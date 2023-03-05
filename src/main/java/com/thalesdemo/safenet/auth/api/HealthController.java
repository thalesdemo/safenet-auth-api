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
 * Controller class for handling requests related to the health of the 
 * SafeNet authentication service.
 *
 * This controller contains endpoints for checking the overall health 
 * status of the gateway and the status of the SafeNet token validator 
 * service.
 *
 * All endpoints in this controller require authentication using the 
 * `X-API-Key`  header. If the request is not authenticated, the controller 
 * returns a 401 Unauthorized response.
 *
 * The endpoints in this controller return responses in JSON format. In case
 * of an  unexpected error, the controller returns a 500 Internal Server Error
 * response. Otherwise, the controller returns a 200 OK response with the 
 * status in the response body.
 * 
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.auth.api;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Controller
@RequestMapping("/api/v1")
@Tag(name = "Health")
public class HealthController {
	
	/**
	 * This logger will be used to log messages in the HealthController class.
	 */
	
	private static final Logger Log = Logger.getLogger(HealthController.class.getName());

	
	/**
	 * An instance of the Authenticate class for use in this controller.
	 */
	
	@Autowired
	private Authenticate api;
	
	
	/**
	 * Check the server status of the SafeNet authentication service.
	 *
	 * This method returns the status of the remote service in JSON format. The status indicates whether the service is up
	 * and running or not. The method requires authentication using the `X-API-Key` header. If the user is not authenticated, 
	 * the method returns a 401 Unauthorized response. If the server status cannot be determined due to an unexpected error, 
	 * the method returns a 500 Internal Server Error response.
	 *
	 * If the server is up, the method returns a 200 OK response with the status in the response body. The response body contains
	 * a JSON object with two properties: `health` and `token_validator`. The `health` property indicates whether the server is up
	 * or down, and can have the value `ok` or `error`, respectively. The `token_validator` property indicates whether the token
	 * validator service is running or not, and can have the value `true` or `false`, respectively.
	 *
	 * Example response for a server that is up:
	 * {
	 *   "health": "ok",
	 *   "token_validator": true
	 * }
	 *
	 * Example response for a server that is down:
	 * {
	 *   "health": "error",
	 *   "token_validator": false
	 * }
	 *
	 * @return A ResponseEntity object containing the HTTP response and response body.
	 */
	
	@GetMapping("/health/check")
	@Operation(
		    summary = "Check the overall health state of this gateway",
		    description = "This API endpoint returns information about the status of the service in JSON format. The response indicates whether the service is up and running or not. The endpoint requires authentication using the `X-API-Key` header. If the request is not authenticated, the method returns a 401 Unauthorized response. If the server status cannot be determined due to an unexpected error, the method returns a 500 Internal Server Error response.\n\n"
		            + "Whether the server is up or down, the endpoint returns a 200 OK response with the status in the response body. The response body contains a JSON object with two properties: `health` and `token_validator`. The `health` property indicates the overall health of the gateway service, and can have the value `ok` or `error`. The `token_validator` property specifically indicates whether the SafeNet token validator service is ready to process requests or not, and can have the value `true` or `false`, respectively.\n\n"
		)
	@ApiResponses(value = {
	    @ApiResponse(responseCode = "200", description = "The request is successful and the health of the service is returned in JSON format.", 
	                 content = @Content(mediaType = "application/json", 
	                 examples = {
	                		 @ExampleObject(name="Operational", description="Example of the service being in an healthy state ", value = ResponseExamples.Health.OK),
	                		 @ExampleObject(name="Malfunctioning", description="Example of the service being in an unhealthy state", value = ResponseExamples.Health.ERROR)
	                		 }        																				
	    )),
	    @ApiResponse(responseCode = "401", description = "You have not authenticated to the API using the header X-API-Key.", content = @Content),
	    @ApiResponse(responseCode = "500", description = "An unexpected error occurred while retrieving the health of the service.", content = @Content)
	})
	
	public ResponseEntity<String> getHealthStatus() {
		boolean tokenValidatorStatus = this.api.getServerStatus();
		
		String healthStatus = tokenValidatorStatus ? "ok" : "error";
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode json = mapper.createObjectNode();
		json.put("health", healthStatus);
		json.put("token_validator", tokenValidatorStatus);

		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			Log.log(Level.SEVERE, "An exception occurred while rendering the health status response", e);
		}
		
		return new ResponseEntity<>(jsonString, HttpStatus.OK);

	}
	
}