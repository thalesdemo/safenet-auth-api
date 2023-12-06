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
 * Provides API key authentication functionality.
 * 
 * Attributes:
 * passwordEncoder (BCryptPasswordEncoder): Encoder used to hash the API key.
 * apiKeyHash (str): Hashed API key stored in the environment variable.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.server.security;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ApiKeyAuthService {

    /**
     * The logger for the ApiKeyAuthService class.
     */

    private static final Logger Log = Logger.getLogger(ApiKeyAuthService.class.getName());

    /**
     * The encoder for encoding the API key hash.
     */

    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * The hash of the API key that is stored in the environment variable.
     */

    private final String apiKeyHash;

    /**
     * Constructor for the ApiKeyAuthService class.
     * 
     * @param passwordEncoder Encoder for encoding the API key hash
     * @param apiKeyHash      Hash of the API key that is stored in the environment
     *                        variable
     */

    public ApiKeyAuthService(BCryptPasswordEncoder passwordEncoder, String apiKeyHash) {
        this.passwordEncoder = passwordEncoder;
        this.apiKeyHash = apiKeyHash;
    }

    /**
     * Checks if the API key in the HTTP request matches the API key hash stored in
     * the environment variable.
     * 
     * @param request HTTP request to check for API key
     * @return true if API key is valid, false otherwise
     */

    public boolean checkApiKey(HttpServletRequest request) {

        // Retrieve the value of the "X-API-Key" header from the incoming HTTP request
        String headerValue = request.getHeader("X-API-Key");

        // If the header value is null, log an error and return false to indicate a
        // failed API key check
        if (headerValue == null) {
            Log.severe("No API key found in header X-API-Key of the HTTP request!");
            return false;
        }

        // Check if the header value only contains ASCII printable characters
        if (!headerValue.matches("^[\\x20-\\x7E]+$")) {
            Log.severe("Found characters NOT allowed in X-API-Key header!");
            return false;
        }

        // Compare the encoded API key from the request with the encoded API key in the
        // environment variable
        boolean apiKeyMatches = passwordEncoder.matches(headerValue, this.apiKeyHash);

        // Logs the result of comparing the API key in the request to the one stored in
        // the environment variable
        Log.fine("Encoding the API key from the request and comparing to the encoded API key in env. Result: "
                + apiKeyMatches);

        // Returns the result of the comparison of the API key in the request to the one
        // stored in the environment variable
        return apiKeyMatches;
    }

}
