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
 * This REST controller provides endpoints for generating and retrieving 
 * GrIDsure grid images that can be used to authenticate users. The controller 
 * also provides a GET endpoint that generates a new grid image for a specified
 * username and returns the image data as a PNG byte array, eliminating the 
 * need of handling the grid challenge string in the client application, 
 * i.e., the challenge-request is made automatically while retrieving the 
 * image, thus logging a challenge event in the SafeNet authentication servers.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.server.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thalesdemo.safenet.auth.api.Authenticate;
import com.thalesdemo.safenet.auth.commons.GridRenderRequest;
import com.thalesdemo.safenet.auth.commons.ResponseExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@Tag(name = "Grid Image")
public class GridImageController {

	/**
	 * This is the logger for the GridImageController class, used to log messages
	 * related to image generation.
	 * The logger instance is static and named after the class.
	 */

	private static final Logger Log = Logger.getLogger(GridImageController.class.getName());

	/**
	 * This is an instance of the Authenticate class that is injected into the
	 * GridImageController constructor.
	 * The Authenticate instance is used to perform user authentication operations.
	 * This instance is non-null and is assigned to a final field called "api".
	 */

	private final Authenticate api;

	/**
	 * Constructs a new instance of the GridImageController class with the specified
	 * Authenticate implementation.
	 * 
	 * @param api An instance of the Authenticate class to be used for
	 *            authentication operations.
	 *            This argument is non-null and is assigned to a final field called
	 *            "api".
	 * 
	 * @throws NullPointerException if the api argument is null.
	 * 
	 * @implNote This constructor is annotated with "@Autowired", which is an
	 *           implicit way of declaring
	 *           a constructor-based dependency injection. The dependency is
	 *           injected by the Spring framework
	 *           when the GridImageController is constructed.
	 */

	public GridImageController(Authenticate api) {
		this.api = api;
	}

	/**
	 * Generates a GrIDsure Base64-encoded PNG image based on the specified
	 * username.
	 * 
	 * @param username The username to generate a challenge for.
	 * 
	 * @return A byte array containing the PNG image data, or an empty array if the
	 *         image data could not be retrieved.
	 *         If the request is successful, the response code is 200 and the image
	 *         data is returned in Base64-encoded PNG format.
	 *         If the request fails due to authentication, the response code is 401.
	 *         If an unexpected error occurs, the response code is 500.
	 * 
	 * @throws RuntimeException if an unexpected error occurs while generating the
	 *                          image data.
	 */

	@GetMapping(value = "/api/v1/authenticate/challenge/grid/{username}", produces = MediaType.IMAGE_PNG_VALUE)
	@Operation(summary = "Generate a GrIDsure Base64-encoded PNG image based on the specified username", description = "This endpoint generates a GrIDsure Base64-encoded PNG image that can be used to authenticate a user. "
			+
			"The image is generated based on the specified username, and can be used to verify that " +
			"the user is authorized to access a protected resource. " +
			"If the request is successful, the image data is returned as a PNG byte array that is Base64-encoded.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The request is successful and the image data is returned in Base64-encoded PNG format.", content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE, schema = @Schema(type = "string", format = "binary"), examples = @ExampleObject(name = "Grid image", value = ResponseExamples.Authentication.GridImage))),
			@ApiResponse(responseCode = "400", description = "The request is malformed and could not be processed correctly.", content = @Content),
			@ApiResponse(responseCode = "401", description = "You have not authenticated to the API using the header X-API-Key.", content = @Content),
			@ApiResponse(responseCode = "500", description = "An unexpected error occurred while generating the image data.", content = @Content)
	})
	public byte[] getImage(
			@Parameter(description = "The unique identifier of the user") @PathVariable("username") String username,
			@RequestBody(required = false) Map<String, String> requestBody) throws Exception {
		// Log the incoming request URL
		String requestUrl = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
		Log.info("Incoming GET " + requestUrl);

		// If the request body is null, create an empty map
		if (requestBody == null) {
			requestBody = new HashMap<>();
		}

		// Get the organization parameter, if present
		Optional<String> organization = Optional.ofNullable(requestBody.get("organization"));

		// Generate the grid image for the specified username and organization (if
		// specified)
		BufferedImage image = this.api.getGridImage(username, organization);

		// Write the image data to a byte array as PNG
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		byte[] imageData = baos.toByteArray();

		// Return the image data
		return imageData;
	}

	/**
	 * Renders a GrIDsure base64-encoded PNG image based on the challenge string
	 * supplied in the request body.
	 *
	 * @param renderGridRequest An object containing the challenge string to be
	 *                          rendered as a base64-encoded PNG image.
	 * 
	 * @return A byte array containing the base64-encoded PNG image data, or an
	 *         empty array if the image data could not be retrieved.
	 *         If the request is successful, the response code is 200.
	 *         If the request fails due to authentication, the response code is 401.
	 *         If the request fails due to a malformed request body, the response
	 *         code is 400.
	 *         If an unexpected error occurs, the response code is 500.
	 * 
	 * @throws Exception if an unexpected error occurs while rendering the image
	 *                   data.
	 */

	@PostMapping(value = "/api/v1/authenticate/render/grid", produces = MediaType.IMAGE_PNG_VALUE)
	@Operation(summary = "Render a GrIDsure base64-encoded PNG image based on the challenge string", description = "This endpoint converts the challenge data supplied in the `string` field into a GrIDsure base64-encoded PNG image.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The request is successful and the GrIDsure base64-encoded PNG image is returned.", content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE, schema = @Schema(type = "string", format = "binary"), examples = @ExampleObject(name = "Grid image", value = ResponseExamples.Authentication.GridImage))),
			@ApiResponse(responseCode = "400", description = "The request is malformed and could not be processed correctly, or the challenge string supplied is invalid.", content = @Content),
			@ApiResponse(responseCode = "401", description = "You have not authenticated to the API using the header X-API-Key.", content = @Content),
			@ApiResponse(responseCode = "500", description = "An unexpected error occurred while rendering the image data.", content = @Content)
	})
	public byte[] getImage(
			@RequestBody GridRenderRequest renderGridRequest) throws Exception {

		// This line gets the current request URL and logs an incoming GET request for
		// debugging purposes.
		String requestUrl = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
		Log.info("Incoming GET " + requestUrl);

		// If the request body is null, create an empty RenderGridRequest to avoid null
		// pointer exceptions
		renderGridRequest = renderGridRequest == null ? new GridRenderRequest() : renderGridRequest;

		// Extract the challenge string from the request body.
		String gridChallenge = renderGridRequest.getString();

		// Create the GrIDsure base64-encoded PNG image from ASCII challenge string
		BufferedImage image = this.api.convertGridDataToImage(gridChallenge);

		// Create a new ByteArrayOutputStream to hold the image data
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Write the image data to the ByteArrayOutputStream as a PNG image
		ImageIO.write(image, "png", baos);

		// Convert the ByteArrayOutputStream to a byte array containing the image data
		byte[] imageData = baos.toByteArray();

		// Return the byte array containing the image data
		return imageData;
	}

}
