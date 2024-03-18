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
package com.thalesdemo.safenet.server.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thalesdemo.safenet.auth.api.Authenticate;
import com.thalesdemo.safenet.token.api.ApiException;
import com.thalesdemo.safenet.token.api.PingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
	 * A reference to the Authenticate bean, which provides methods for
	 * authenticating API requests and checking the status
	 * of the SafeNet authentication service.
	 * 
	 * This field is autowired by Spring for use in this controller. The
	 * Authenticate bean is used by the endpoints in this
	 * controller to retrieve the status of the service
	 */

	@Autowired
	private Authenticate api;

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
	 * Check the overall health status of the SafeNet authentication service and
	 * the connectivity to the Bsidca server.
	 *
	 * The overall health status (`health`) is determined based on the status
	 * of the SafeNet authentication service (`token_validator`) and the
	 * connectivity status of the Bsidca server (`bsidca_soap_api`). The overall
	 * health
	 * status will be "ok" only if both services are operational. Otherwise,
	 * it will be "error".
	 * 
	 * This method returns the status in JSON format and requires authentication
	 * using the `X-API-Key` header. If the user is not authenticated, the method
	 * returns a 401 Unauthorized response. In case of unexpected errors, it
	 * returns a 500 Internal Server Error response.
	 *
	 * Example responses:
	 * - All services operational:
	 * {
	 * "health": "ok",
	 * "token_validator": true,
	 * "bsidca_soap_api": true
	 * }
	 *
	 * - Any service not operational:
	 * {
	 * "health": "error",
	 * "token_validator": false,
	 * "bsidca_soap_api": false
	 * }
	 *
	 * @return The health status of the services as a HealthResponse object.
	 */

	@GetMapping("/health/check")
	@Operation(summary = "Check the overall health state of this gateway", description = "This API endpoint returns the overall health status of the SafeNet authentication service and the connectivity to the Bsidca server in JSON format. The overall health (`health`) will be \"ok\" only if both the SafeNet authentication service (`token_validator`) and the Bsidca server (`bsidca_soap_api`) are operational. Otherwise, it will be \"error\". The endpoint requires authentication using the `X-API-Key` header. If the request is not authenticated, the method returns a 401 Unauthorized response. If the server status cannot be determined due to an unexpected error, the method returns a 500 Internal Server Error response.\n\n"
			+ "Whether the server is up or down, the endpoint returns a 200 OK response with the status in the response body. The response body contains a JSON object with properties: `health`, `token_validator`, and `bsidca_soap_api`. The `health` property indicates the overall health of the gateway service, and can have the value `ok` or `error`. The `token_validator` property specifically indicates whether the SafeNet token validator service is ready to process requests or not, and can have the value `true` or `false`, respectively. The `bsidca_soap_api` property indicates the connectivity status to the Bsidca server.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The request is successful and the health of the service is returned in JSON format.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthResponse.class), examples = {
					@ExampleObject(name = "Operational", description = "Example of the service being in an healthy state and Bsidca server is reachable", value = "{\"health\":\"ok\",\"token_validator\":true,\"bsidca_soap_api\":true}"),
					@ExampleObject(name = "Malfunctioning", description = "Example of the service being in an unhealthy state or Bsidca server is not reachable", value = "{\"health\":\"error\",\"token_validator\":false,\"bsidca_soap_api\":false}")
			})),
			@ApiResponse(responseCode = "400", description = "The request was invalid or incomplete, possibly due to malformed JSON data.", content = @Content(schema = @Schema(type = "object", additionalProperties = Schema.AdditionalPropertiesValue.TRUE))),
			@ApiResponse(responseCode = "401", description = "You have not authenticated to the API using the header X-API-Key.", content = @Content),
			@ApiResponse(responseCode = "500", description = "An unexpected error occurred while retrieving the health of the service.", content = @Content)
	})

	public ResponseEntity<HealthResponse> getHealthStatus() {
		boolean tokenValidatorStatus = this.api.getServerStatus();
		// TODO: fix parseBoolean since the values from config are either "POST" or
		// "GET"
		boolean useGetMethodFromConfig = Boolean
				.parseBoolean(env.getProperty("safenet.bsidca.scheduling.ping-method", "false"));

		boolean bsidcaPingStatus = false;
		try {
			ResponseEntity<Object> pingResponse = bsidcaPingService.handlePing(useGetMethodFromConfig);
			bsidcaPingStatus = (pingResponse.getStatusCode().value() == 200);
			if (!bsidcaPingStatus) {
				Log.log(Level.WARNING, "Error while pinging BSIDCA: {0}", pingResponse.getBody());
			}
		} catch (ApiException ex) {
			Log.log(Level.WARNING, "Error while pinging BSIDCA: {0}", ex.getMessage());
		}

		// Check overall health based on both statuses
		boolean overallHealth = tokenValidatorStatus && bsidcaPingStatus;
		String healthStatus = overallHealth ? "ok" : "error";

		HealthResponse response = new HealthResponse();
		response.setHealth(healthStatus);
		response.setToken_validator(tokenValidatorStatus);
		response.setBsidca_soap_api(bsidcaPingStatus);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}