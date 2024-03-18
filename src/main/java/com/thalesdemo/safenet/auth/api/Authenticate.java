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
 * Authenticate class provides methods for authentication using SafeNet/CRYPTOCard API.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.auth.api;

import CRYPTOCard.API.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thalesdemo.safenet.auth.commons.AuthenticationChallenge;
import com.thalesdemo.safenet.auth.commons.AuthenticationRequest;
import com.thalesdemo.safenet.auth.commons.AuthenticationResponse;
import com.thalesdemo.safenet.auth.commons.ResponseCode;

public class Authenticate {

	/**
	 * The logger for the Authenticate class.
	 */

	private static final Logger Log = Logger.getLogger(Authenticate.class.getName());

	/**
	 * Defines response codes for the CRYPTOCard API.
	 */

	private class RC {
		public static final int AUTH_FAILURE = 0;
		public static final int AUTH_SUCCESS = 1;
		public static final int CHALLENGE = 2;
		public static final int SERVER_PIN_PROVIDED = 3;
		public static final int USER_PIN_CHANGE = 4;
		public static final int OUTER_WINDOW_AUTH = 5;
		public static final int CHANGE_STATIC_PASSWORD = 6;
		public static final int STATIC_CHANGE_FAILED = 7;
		public static final int PIN_CHANGE_FAILED = 8;
	}

	/**
	 * The CRYPTOCard API instance.
	 */

	private CRYPTOCardAPI api;

	/**
	 * The path to the INI file for the CRYPTOCard API configuration.
	 */

	private String iniPath;

	/**
	 * The name of the virtual server for the CRYPTOCard API.
	 *
	 * Note: This field is no longer used by CRYPTOCard.API.
	 */

	private String organization;

	/**
	 * Creates a new instance of the Authenticate class with the specified
	 * organization and INI file path.
	 *
	 * @param organization the name of the virtual server for the CRYPTOCard API
	 * @param iniPath      the path to the INI file for the CRYPTOCard API
	 *                     configuration
	 */

	public Authenticate(String organization, String iniPath) {
		// Set up the CRYPTOCard API instance, INI file path, and organization
		this.api = CRYPTOCardAPI.getInstance();
		this.iniPath = iniPath;
		this.organization = organization;

		// Set the INI file path for the CRYPTOCard API
		this.api.setINIPath(this.iniPath);

		// Load the required JNI library for the CRYPTOCard API
		try {
			this.api.LoadJNILibrary();
			// Log a message to indicate that initialization is complete
			Log.info("Constructor initialization complete.");
		} catch (UnsatisfiedLinkError ex) {
			// Log an error message and the entire stack trace if the JNI library could not
			// be loaded
			Log.log(Level.SEVERE, "Failed to load JNI library", ex);
		} catch (Exception ex) {
			// Log an error message and the entire stack trace if there is an exception
			// while loading the JNI library
			Log.log(Level.SEVERE, "Exception while loading JNI library", ex);
		}
	}

	/**
	 * Sends an authentication request to the CRYPTOCard API server with the
	 * specified username, passcode, and state.
	 *
	 * @param username          the username to authenticate
	 * @param passcode          the passcode to use for authentication
	 * @param state             the state of the authentication request
	 * @param inputOrganization (optional) the name of the virtual server for the
	 *                          CRYPTOCard API
	 * @return an AuthenticationResponse object representing the server's response
	 */

	private AuthenticationResponse sendToServerAuthenticate(String username, String passcode, String state,
			Optional<String> inputOrganization) {

		// Create a new AuthenticationResponse object for the specified username
		AuthenticationResponse response = new AuthenticationResponse(username);

		// Set the organization to use for authentication, either the input organization
		// or the default organization
		String effectiveOrganization = inputOrganization.orElse(this.organization);

		// Log information about the received username, passcode, and organization
		Log.log(Level.FINEST, "Received username: {0}",  username);
		Log.log(Level.FINEST, "Received passcode: {0}",  passcode);
		Log.log(Level.FINEST, "Received org name: {0}", effectiveOrganization);

		// If the passcode is empty or null, log a warning and trigger a
		// challenge-response
		if (passcode == null || passcode.isEmpty()) {
			Log.warning("Empty passcode has been detected. Triggering challenge-response.");
		}

		// If the username is "anonymousUser", check the state of the authentication
		// request
		if (username.equals("anonymousUser")) {
			// If the state is "FAKE_CHALLENGE", log a warning and return an
			// AuthenticationResponse with AUTH_FAILURE
			if (state.equals(ResponseCode.FAKE_CHALLENGE.getName())) {
				Log.warning(
						"Discarding and rejecting authentication request since this is the second fake challenge attempt");
				return new AuthenticationResponse(username, ResponseCode.AUTH_FAILURE);
			}
			// If the state is not "FAKE_CHALLENGE", log a warning and return an
			// AuthenticationResponse with a fake challenge
			else {
				Log.warning(
						"Discarding and issuing fake authentication challenge since this is the first challenge attempt");
				AuthenticationChallenge fakeChallenge = new AuthenticationChallenge(
						ResponseCode.FAKE_CHALLENGE.getName(), this.getFakeGridChallenge(),
						ResponseCode.FAKE_CHALLENGE.getName());
				return new AuthenticationResponse(username, ResponseCode.FAKE_CHALLENGE, fakeChallenge);
			}
		}

		// Create an array of strings to hold the request data and response values
		String[] arrData = new String[11];

		// Add the input values to the array
		arrData[0] = username; // username (input)
		arrData[1] = effectiveOrganization; // organization (input)
		arrData[2] = passcode; // passcode (input)
		arrData[10] = ""; // client IP address (input)

		// Add placeholders for the output values
		arrData[3] = ""; // challenge (output)
		arrData[4] = state; // state in (output)
		arrData[5] = ""; // challenge data (output)
		arrData[6] = ""; // challenge string (output)
		arrData[7] = ""; // authentication result - value from 0 to 8 (output)
		arrData[8] = ""; // authentication servers health error - 0 for up, 1 for down (output)
		arrData[9] = ""; // Log message (output)

		try {
			// Call the Authenticate method of the CRYPTOCard API with the request data
			this.api.Authenticate(arrData);

			// Extract the response data from the arrData array
			String rawMsg = arrData[9];
			int status = Integer.parseInt(arrData[7]);
			String challengeName = arrData[5];
			String challengeState = arrData[4];
			String challengeData = arrData[3];

			// Log information about the server response
			Log.log(Level.FINE, "Server response:");
			Log.log(Level.FINE, "status = {0}", status);
			Log.log(Level.FINE, "challengeName = {0}", challengeName);
			Log.log(Level.FINE, "challengeData: grid ascii string = {0}", challengeData);
			Log.log(Level.FINE, "challengeState: output_state -> challenge state variable = {0}", challengeState);
			Log.log(Level.FINE, "rawMsg: raw log message = {0}", rawMsg);

			// Create a new AuthenticationChallenge object and set its properties based on
			// the response data
			response.setStatus(status)
					.setChallenge(
							new AuthenticationChallenge()
									.setChallengeName(challengeName)
									.setChallengeData(challengeData)
									.setState(challengeState));

			// Handle the response status using a switch statement
			switch (status) {
				case RC.AUTH_SUCCESS:
					Log.log(Level.INFO, "Authentication success for user: {0}", username);
					break;
				case RC.AUTH_FAILURE:
				case RC.PIN_CHANGE_FAILED:
				case RC.STATIC_CHANGE_FAILED:
					Log.log(Level.INFO, "Authentication denied for user: {0}", username);
					break;
				case RC.CHALLENGE:
					Log.log(Level.INFO, "Registered an authentication challenge for user: {0}", username);
					break;
				case RC.SERVER_PIN_PROVIDED:
					Log.log(Level.INFO, "Encountered a case of Server PIN Provided.");
					break;
				case RC.USER_PIN_CHANGE:
					break;
				case RC.CHANGE_STATIC_PASSWORD:
					break;
				case RC.OUTER_WINDOW_AUTH:
					break;
				default:
					Log.warning("Unresolved server response (" + status + ") for user: " + username);
			}

		} catch (Exception e) {
			// If an exception occurs, log an error and print the stack trace
			Log.log(Level.SEVERE, "An error occurred at Authenticate.sendToServerAuthenticate(): ", e);
		}

		// Return the AuthenticationResponse object
		return response;

	}

	/**
	 * Validates a passcode for the specified username and state.
	 *
	 * @param username     the username to validate
	 * @param passcode     the passcode to validate
	 * @param state        the state of the authentication request
	 * @param organization (optional) the name of the virtual server for the
	 *                     CRYPTOCard API
	 * @return an AuthenticationResponse object representing the server's response
	 */

	public AuthenticationResponse validateCode(String username, String passcode, String state,
			Optional<String> organization) {
		// Call sendToServerAuthenticate to send an authentication request to the
		// CRYPTOCard API server
		return this.sendToServerAuthenticate(username, passcode, state, organization);
	}

	/**
	 * Retrieves the GrIDsure challenge data for the specified username and state.
	 *
	 * @param username     the username to retrieve the challenge data for
	 * @param state        the state of the authentication request
	 * @param organization (optional) the name of the virtual server for the
	 *                     CRYPTOCard API
	 * @return the GrIDsure challenge data
	 */

	public String getGridChallengeData(String username, String state, Optional<String> organization) {

		// Call sendToServerAuthenticate to send an authentication request to the
		// CRYPTOCard API server
		AuthenticationResponse result = this.sendToServerAuthenticate(username, "g", state, organization);

		// Retrieve the challenge data and challenge name from the server's response
		String challengeData = result.getChallenge().getChallengeData();
		String challengeName = result.getChallenge().getChallengeName();

		// If the challenge name is "GrIDsure", return the challenge data
		if (challengeName.equalsIgnoreCase("gridsure")) {
			return challengeData;
		}

		// If the challenge name is not "GrIDsure", return a fake grid challenge
		return this.getFakeGridChallenge();
	}

	/**
	 * Generates fake grid challenge data.
	 *
	 * @return a string containing fake grid challenge data
	 */

	private String getFakeGridChallenge() {
		/*
		 * TODO: Make an option to switch to offer generation of random valid data.
		 * For now, we are returning a hardcoded fake data.
		 */
		final String fake_data = ".............G4M3..0V3R.............";
		return fake_data;
	}

	/**
	 * Converts a GridSure challenge string to a BufferedImage object.
	 * 
	 * @param challenge the GridSure challenge string to convert
	 * @return a BufferedImage object representing the GridSure challenge, or an
	 *         empty image if an error occurs
	 */

	public BufferedImage convertGridDataToImage(String challenge) {
		try {
			return this.api.getGridSureGrid(challenge);
		} catch (Exception e) {
			Log.log(Level.SEVERE, "An error occurred while converting grid string to image: ", e);
			return new BufferedImage(0, 0, 0);
		}
	}

	/**
	 * Retrieves the GrIDsure challenge for the specified user and converts it to a
	 * BufferedImage object.
	 *
	 * @param username     the username of the user whose GrIDsure challenge to
	 *                     retrieve
	 * @param organization (optional) the name of the virtual server for the
	 *                     CRYPTOCard API
	 * @return a BufferedImage object representing the user's GrIDsure challenge
	 *         If an error occurs while converting the GrIDsure challenge to an
	 *         image, an empty image with a size of 0 x 0 is returned.
	 */

	public BufferedImage getGridImage(String username, Optional<String> organization) {
		String gridData = this.getGridChallengeData(username, "", organization);
		return this.convertGridDataToImage(gridData);
	}

	/**
	 * Validates the authentication request by sending it to the CRYPTOCard API
	 * server.
	 *
	 * @param authenticationRequest the authentication request to be validated
	 * @return an AuthenticationResponse object representing the server's response
	 */

	public AuthenticationResponse validateCode(AuthenticationRequest authenticationRequest) {
		// Call the sendToServerAuthenticate method with the username, passcode, state,
		// and organization fields from the authentication request
		return this.sendToServerAuthenticate(authenticationRequest.getUsername(),
				authenticationRequest.getCode(),
				authenticationRequest.getState(),
				Optional.ofNullable(authenticationRequest.getOrganization()));
	}

	/**
	 * Returns a boolean value indicating whether the server is up or down.
	 *
	 * This method calls the checkServerStatus() method on the object api with an
	 * array arrData as a parameter.
	 * The checkServerStatus() method is assumed to update the arrData array with
	 * status information.
	 * If checkServerStatus() throws an exception, the method logs an error message
	 * at the SEVERE level
	 * and returns false to indicate that the server status is in an error state.
	 *
	 * If checkServerStatus() completes successfully, the method uses the
	 * Objects.equals() method to check if the
	 * value at index 8 of the arrData array is equal to the string "0" (in
	 * accordance to CRYPTOCardAPI's manual).
	 * If it is, the method returns true to indicate that the server is up.
	 * Otherwise, it returns false to
	 * indicate that the server is down.
	 *
	 * @return A boolean value indicating whether the server is up or down.
	 */

	public boolean getServerStatus() {
		String[] arrData = new String[11];
		try {
			this.api.checkServerStatus(arrData);
		} catch (Exception e) {
			// Log an error message at the SEVERE level if checkServerStatus() throws an
			// exception
			Log.log(Level.SEVERE, "Failed to check server status: {0}", e.getMessage());
			Log.severe("Trace: " + Arrays.toString(arrData));
			return false;
		}
		// Use Objects.equals() to check if the value at index 8 of the arrData array is
		// "0"
		boolean serverReady = Objects.equals(arrData[8], "0");

		if (!serverReady)
			Log.warning("Error during getServerStatus(): " + Arrays.toString(arrData));

		return serverReady;
	}

}