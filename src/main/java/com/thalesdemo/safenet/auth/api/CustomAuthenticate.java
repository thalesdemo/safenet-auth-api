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
 * A class that provides custom authentication using the SafeNet Authentication 
 * TokenValidator API.
 * This class handles the process of authenticating users using the push 
 * notification authentication method.
 * The authentication process begins with a request for a challenge by the user. 
 * The challenge is then sent to the user's device as push notification.
 * The user then responds to the push notification to complete the authentication
 * process. The response is sent back to the TokenValidator API for validation 
 * and a response is returned indicating whether the authentication was successful
 * or not.
 * This class utilizes the TokenValidatorRequestDTO and TokenValidatorResponseDTO 
 * classes to construct and send requests to the TokenValidator API, as well as 
 * to parse and handle responses. It also utilizes the* AuthenticationChallenge 
 * and AuthenticationResponse classes to handle challenges and responses during 
 * the authentication process.
 * In addition, this class contains methods for handling redirects, timeouts, 
 * and error responses during the authentication process.
 *
 * @see TokenValidatorRequestBuilder
 * @see TokenValidatorRequestDTO
 * @see TokenValidatorResponseDTO
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.auth.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safenet.keycloak.agent.tokenvalidatoradapter.dto.tv.TVRequestCredentialType;
import com.safenet.keycloak.agent.tokenvalidatoradapter.dto.tv.TVRequestType;
import com.safenet.keycloak.agent.tokenvalidatoradapter.dto.tv.TokenValidatorRequestDTO;
import com.safenet.keycloak.agent.tokenvalidatoradapter.dto.tv.TokenValidatorResponseDTO;
import com.safenet.keycloak.agent.tokenvalidatoradapter.dto.tv.TokenValidatorReturn;
import com.safenet.keycloak.agent.tokenvalidatoradapter.dto.tv.TokenValidatorRequestDTO.TokenValidatorRequestBuilder;
import com.thalesdemo.safenet.auth.commons.AuthenticationChallenge;
import com.thalesdemo.safenet.auth.commons.AuthenticationResponse;
import com.thalesdemo.safenet.auth.commons.ResponseCode;



public class CustomAuthenticate {

    /**
     * The push trigger character used with the push notification authentication method
     * in the TokenValidator API.
     */

    private static final String PUSH_TRIGGER_CHAR = "p";


    /**
     * The connect and read timeout for the RestTemplate.
     */

    private static final int READ_TIMEOUT = 60000;
    private static final int CONNECT_TIMEOUT = 10000;


    /**
     * The logger for the CustomAuthenticate class.
     */

    private static final Logger Log = Logger.getLogger(CustomAuthenticate.class.getName());


    /**
     * The resource name for the TokenValidator API.
     */

    private String resourceName;


    /**
     * The primary and failover authentication URLs for the TokenValidator API.
     */

    private String primaryAuthUrl;
    private String failoverAuthUrl;


    /**
     * The agent key for the TokenValidator API.
     */

    private String agentKey;


    /**
     * The CustomTokenValidatorWrapperImpl used to make the TokenValidator calls.
     */

    private CustomTokenValidatorWrapperImpl tokenValidator;


    /**
     * The builder for the CustomAuthenticate class.
     * 
     * @return the custom authenticate object
     */

    public CustomAuthenticate build() {
        this.tokenValidator = new CustomTokenValidatorWrapperImpl(this.primaryAuthUrl, this.failoverAuthUrl,
                this.agentKey);
        return this;
    }


    /**
     * Sets the primary authentication URL for the TokenValidator API.
     * @param primaryAuthUrl the primary authentication URL
     * @return the custom authenticate object
     */

    public CustomAuthenticate setPrimaryAuthUrl(String primaryAuthUrl) {
        this.primaryAuthUrl = primaryAuthUrl;
        return this;
    }


    /**
     * Sets the failover authentication URL for the TokenValidator API.
     * @param failoverAuthUrl the failover authentication URL
     * @return the custom authenticate object
     */

    public CustomAuthenticate setFailoverAuthUrl(String failoverAuthUrl) {
        this.failoverAuthUrl = failoverAuthUrl;
        return this;
    }


    /**
     * Sets the agent key for the TokenValidator API.
     * @param agentKey the agent key
     * @return the custom authenticate object
     */

    public CustomAuthenticate setAgentKey(String agentKey) {
        this.agentKey = agentKey;
        return this;
    }


    /**
     * Sets the resource name for the TokenValidator API.
     * @param resourceName the resource name
     * @return the custom authenticate object
     */

    public CustomAuthenticate setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }


    /**
     * Authenticates the user with the TokenValidator API using push notification.
     *
     * @param username the username to authenticate.
     * @param userIp the IP address of the user requesting authentication.
     * @param authIdUrl the authentication ID URL obtained from the previous push challenge, or null to generate a new challenge.
     * @param mode the push mode: "one-step" (quicklog) or "challenge-response".
     * @return an AuthenticationResponse object containing the authentication result and any associated challenge data.
     *
     * <p>If authIdUrl is null or empty, a new push challenge is generated by calling {@link #pushChallengeRequest(String, String)} with the username and userIp. 
     * If mode is "challenge-response", the authentication response includes the base64-encoded authIdUrl in the challenge state, which the client will need to decode later.
     * If authIdUrl is not null, it is assumed to be the result of a previous push challenge and is decoded from base64, if necessary.
     * The pushParkingService is then polled repeatedly to get the authStatus until either a valid response is obtained, or a timeout occurs.
     * If authStatus is not null, it is passed along with the authId and other parameters to {@link #pushChallengeResponse(String, String, String, String)}.
     * If the authentication is successful, the AuthenticationResponse object contains a success response code, otherwise it contains an authentication failure response code.
     * </p>
     */

    public AuthenticationResponse pushOTP(String username, String userIp, String authIdUrl, String mode) {

        AuthenticationResponse response = new AuthenticationResponse(username, ResponseCode.AUTH_FAILURE);

        if (authIdUrl == null || authIdUrl.isEmpty()) {
            // Generate push challenge to get the authIdUrl
            authIdUrl = pushChallengeRequest(username, userIp);

            // If the mode is challenge-response, return the authIdUrl in base64
            if ("challenge-response".equalsIgnoreCase(mode)) {
                String base64EncodedAuthIdUrl = Base64.getEncoder().encodeToString(authIdUrl.getBytes());
                AuthenticationChallenge challengeData = new AuthenticationChallenge().setChallengeName("push_otp")
                        .setState(base64EncodedAuthIdUrl);
                return new AuthenticationResponse(username, ResponseCode.AUTH_CHALLENGE).setChallenge(challengeData); 
            }
        } else {
            // Decode the authIdUrl from base64
            authIdUrl = new String(Base64.getDecoder().decode(authIdUrl));
        }

        // Check if the authId is null or empty
        if (authIdUrl == null || authIdUrl.isEmpty()) {
            return response;
        }

        // Extract the authentication Id from the authIdUrl
        String authId = authIdUrl.substring(authIdUrl.lastIndexOf("/") + 1);

        // Poll the pushParkingService to get the authStatus
        String authStatus = pushParkingService(authIdUrl);

        // Send the authStatus to the TokenValidator if it is not null
        if (authStatus != null && this.pushChallengeResponse(username, authId, authStatus, userIp)) {
            response.setResponse(ResponseCode.AUTH_SUCCESS);
        }

        return response;
    }


    /**
     * Handles a temporary redirect response from the SafeNet Cloud parking service by constructing an absolute redirect
     * URL and executing a POST request to the redirected URL.
     *
     * @param request The HTTP request that resulted in the temporary redirect response.
     * @param body The body of the HTTP request.
     * @param execution The execution of the HTTP request.
     * @return The HTTP response for the redirected POST request.
     * @throws IOException If an I/O error occurs while constructing the absolute redirect URL.
     */

    private ClientHttpResponse handleRedirect(org.springframework.http.HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        if (response.getStatusCode() == HttpStatus.TEMPORARY_REDIRECT) {
            URI location = response.getHeaders().getLocation();
            if (location != null && !location.isAbsolute()) {
                try {
                    URI requestUri = request.getURI();
                    location = new URI(requestUri.getScheme(), requestUri.getAuthority(), location.getPath(),
                            location.getQuery(), location.getFragment());
                    
                    Log.log(Level.FINE, "Following Redirect URL: {0}", location);
                    return restTemplateWithRedirect().execute(location, HttpMethod.POST,
                            req -> req.getHeaders().putAll(request.getHeaders()), clientHttpResponse -> clientHttpResponse);

                } catch (URISyntaxException e) {
                    String errorMsg = String.format("Failed to construct the absolute redirect URL. Request URI: %s. Error: %s", request.getURI(), e.getMessage());
                    Log.log(Level.SEVERE, errorMsg);
                    throw new IOException(errorMsg);
                }
            }
        }
        return response;
    }


    /**
     * Returns a custom response error handler that ignores temporary redirect responses (status code 307). 
     * All other responses will be handled by the default response error handler.
     * 
     * @return A response error handler that ignores temporary redirect responses.
     */

    private ResponseErrorHandler noRedirectErrorHandler() {
        return new DefaultResponseErrorHandler() {
            @Override
            public void handleError(@NonNull ClientHttpResponse response) throws IOException {
                if (response.getStatusCode() != HttpStatus.TEMPORARY_REDIRECT) {
                    super.handleError(response);
                }
            }
        };
    }


    /**
     * Returns a custom `RestTemplate` object that includes a request factory with specified connection and read timeouts, 
     * as well as an HTTP interceptor that handles temporary redirect responses by constructing an absolute redirect URL and 
     * executing a POST request to the redirected URL. 
     * All other responses will be handled by the default response error handler.
     * 
     * @return A custom `RestTemplate` object with timeouts and redirect handling.
     */

    private RestTemplate restTemplateWithRedirect() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT); // set connection timeout to 10 seconds
        requestFactory.setReadTimeout(READ_TIMEOUT); // set read timeout to 60 seconds
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(this::handleRedirect); // Replaced lambda with method reference
        restTemplate.setErrorHandler(noRedirectErrorHandler());
        return restTemplate;
    }
    

    /**
     * Sends a POST request to the specified authentication ID URL using a custom `RestTemplate` object, which includes
     * a request factory with specified timeouts and redirect handling, and returns the HTTP response as a 
     * `ResponseEntity<String>` object. 
     * 
     * If the request results in a 404 error (NOT_FOUND), this method returns null. 
     * 
     * If the request results in a resource access error due to timeout or partial content, this method returns a 
     * `ResponseEntity<String>` object with a status code of 206 (PARTIAL_CONTENT). This is a workaround to handle
     * the parking service abruptly closing the connection when the request is approved after the first redirect and to
     * tell the client to retry the request again. If the request is approved, the second request will result in a 200
     * and contain the response status string.
     *
     * @param authIdUrl The authentication ID URL to send the POST request to.
     * @return A `ResponseEntity<String>` object representing the HTTP response, or null if the request results in a
     * 404 error (NOT_FOUND).
     */

    private ResponseEntity<String> postParkingService(String authIdUrl) {

        // create the RestTemplate with the request factory
        RestTemplate restTemplate = restTemplateWithRedirect();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Log.log(Level.FINE, "Push OTP Auth ID URL: {0}", authIdUrl);

        ResponseEntity<String> response = null;

        try {
            // Send a POST request to the authentication ID URL
            response = restTemplate.postForEntity(authIdUrl, null, String.class);
        } catch (HttpClientErrorException ex) {
            // If the response status code is HttpStatus.NOT_FOUND, return null
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                Log.log(Level.FINE, ex.getMessage());
                return null;
            } else {
                return new ResponseEntity<>(ex.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            // Logs a message with Level FINE indicating that an exception occurred in pushParkingService method.
            // Possible causes are push timeout or response available on SPS parking server.
            // The error message is also logged.
            Log.log(Level.FINE,
                    "pushParkingService exception: possible causes are push timeout or response available on SPS parking server. ");
            Log.log(Level.FINE, e.getMessage());

            // Return a 206 status code to indicate partial content to attempt a second call in case of response availability
            return new ResponseEntity<>(HttpStatus.PARTIAL_CONTENT); 
        }

        return response;
    }


    /**
     * Builds a Token Validator request to verify user credentials and initiates a push challenge by setting the password 
     * to the predefined PUSH_TRIGGER_CHAR value. 
     * Sends the request to the Token Validator service and returns the push OTP authentication ID URL if the response 
     * is successful. 
     * Logs details of the request and response, including the Token Validator request type and credential type.
     *
     * @param username The username to authenticate.
     * @param userIp The IP address of the user requesting authentication.
     * @return A string representing the push OTP authentication ID URL if the response is successful, or an empty string 
     * if the response is null.
     */

    private String pushChallengeRequest(String username, String userIp) {
        // Build Token Validator request with the provided parameters
        TokenValidatorRequestBuilder builder = new TokenValidatorRequestBuilder();
        builder.resourceName(this.resourceName);
        builder.username(username);
        builder.userIpAddress(userIp);
        builder.password(PUSH_TRIGGER_CHAR);
        builder.tvRequestType(TVRequestType.verifycredentials);
        builder.tvRequestCredentialType(TVRequestCredentialType.TokenValidation);
        TokenValidatorRequestDTO requestDTO = builder.build();

        // Log details of the request
        String logDetailsRequest = requestDTO != null ? requestDTO.toString() : "null";
        Log.fine("pushChallengeRequest - requestDTO:");
        Log.fine(logDetailsRequest);

        // Send the Token Validator request and log details of the response
        TokenValidatorResponseDTO responseDTO = this.tokenValidator.authenticate(requestDTO);
        String logDetailsResponse = responseDTO != null ? responseDTO.toString() : "null";
        Log.fine("pushChallengeRequest - responseDTO:");
        Log.fine(logDetailsResponse);

        // Return the push OTP authentication ID URL if the response is successful
        return responseDTO != null ? responseDTO.getPushOtpAuthId() : "";
    }


    /**
     * Sends a request to the SafeNet Parking Service (SPS) and retrieves the response. 
     * The method checks the status field of the response body and returns its value if it is not empty, otherwise 
     * returns null.
     * If the response is null or if a partial content status is received, the method retries the request once again.
     * Logs details of the request and response, including the response status and body.
     *
     * @param authIdUrl The push OTP authentication ID URL for which to send the request.
     * @return A string representing the value of the status field of the response body if it is not empty, or null if 
     * it is empty or if the response is null.
     */

    private String pushParkingService(String authIdUrl) {

        ResponseEntity<String> response = postParkingService(authIdUrl);

        if (response == null || response.getStatusCode().equals(HttpStatus.PARTIAL_CONTENT)) {
            Log.fine(
                    "Connection closed. Timeout expired on SPS parking server OR response available. Calling pushParkingService once again.");
            response = postParkingService(authIdUrl);
        }

        if(response == null){
            Log.fine("Invalid response from SPS parking server. Response is null, thus pushParkingService(authIdUrl) is now returning null.");
            return null;
        }

        String responseBody = response.getBody();
        Log.fine(response.getStatusCode() + " " + response.getBody());

        ObjectMapper mapper = new ObjectMapper();
        try {

            // Check if the responseBody is null
            JsonNode json = null;
            if (responseBody == null) {
                // Create a JSON object with a status key-value pair
                json = mapper.createObjectNode();
            } else {
                json = mapper.readTree(responseBody);
            }

            /*
             * Check if the status field is not empty. If it is not empty, return the value of the status field. If it is
             * empty, return null.
             */
            if (!json.get("status").asText().isEmpty()) {
                Log.log(Level.FINE, "Received STATUS in parking server response:\n{0}", json);
                return json.get("status").asText();
            } else {
                Log.fine("Have received NO STATUS in parking server response");
            }

        } catch (JsonMappingException e) {
            Log.log(Level.SEVERE, "JsonMappingException in pushParkingService", e);
        } catch (JsonProcessingException e) {
            Log.log(Level.SEVERE, "JsonProcessingException in pushParkingService", e);
        }

        return null;
    }


    /**
    * Builds a Token Validator request to respond to a push challenge with the given authentication ID,
    * authentication status, and user details.
    * Sends the request to the Token Validator service and returns true if the response
    * is successful and the authentication status is AUTH_SUCCESS.
    * Logs details of the request and response, including the Token Validator request type and credential type.
    *
    * @param username The username to authenticate.
    * @param authId The push OTP authentication ID.
    * @param authStatus The authentication status of the push challenge.
    * @param userIp The IP address of the user requesting authentication.
    * @return True if the response is successful and the authentication status is AUTH_SUCCESS, false otherwise.
    */

    private boolean pushChallengeResponse(String username, String authId, String authStatus, String userIp) {
        // Build Token Validator request with the provided parameters
        TokenValidatorRequestBuilder builder = new TokenValidatorRequestBuilder();
        builder.username(username);
        builder.password(PUSH_TRIGGER_CHAR);
        builder.userIpAddress(userIp);
        builder.pushOtpAuthId(authId);
        builder.pushOtpSpsStatus(authStatus);
        builder.tvRequestType(TVRequestType.verifycredentials);
        builder.tvRequestCredentialType(TVRequestCredentialType.TokenValidation);
        TokenValidatorRequestDTO requestDTO = builder.build();

        // Log details of the request
        String logDetailsRequest = requestDTO != null ? requestDTO.toString() : "null";
        Log.fine("pushChallengeResponse - requestDTO:");
        Log.fine(logDetailsRequest);

        // Send request to the Token Validator service and get the response
        TokenValidatorResponseDTO responseDTO = this.tokenValidator.authenticate(requestDTO);

        // Log details of the response
        String logDetailsResponse = responseDTO != null ? responseDTO.toString() : "null";
        Log.fine("pushChallengeResponse - responseDTO:");
        Log.fine(logDetailsResponse);

        // Check if the authentication was successful based on the response
        boolean authSuccess = responseDTO != null && Integer.toString(TokenValidatorReturn.AUTH_SUCCESS.getValue())
                .equals(responseDTO.getReturnValue());

        // Log the authentication result
        Log.log(Level.FINE, "-> Push authentication successful? {0}", authSuccess);

        return authSuccess;
    }

}
