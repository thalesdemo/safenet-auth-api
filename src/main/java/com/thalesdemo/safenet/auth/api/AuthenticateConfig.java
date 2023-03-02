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
 * This class provides the configuration for the Authenticate class.
 * It registers the configuration in the @Bean Authenticate authenticate() and sets the INI path and
 * organization from environment variables. If the environment variables are not set, the default values are used.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.auth.api;

import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration 
public class AuthenticateConfig {
	
	/**
	 * The logger instance used to log messages in this class.
	 */
	
	private static final Logger Log = Logger.getLogger(AuthenticateConfig.class.getName());

	
	/**
	 * The path to the INI file. If the JCRYPTO_INI_PATH environment variable is not set,
	 * the default value '/app/config/config.ini' is used.
	 */
	
	private String JCRYPTO_INI_PATH;

	
    /**
     * Creates a new instance of the Authenticate object and registers it as a bean
     * so that it can be used by other components.
     * 
     * @return an instance of the Authenticate object
     * @throws Exception if an error occurs while creating the Authenticate object
     */
	
    @Bean
    Authenticate authenticate() throws Exception {
       Log.info("Registering configuration in @Bean Authenticate authenticate()...");
        
        // Check if the JCRYPTO_INI_PATH environment variable is set
        JCRYPTO_INI_PATH = Optional.ofNullable(System.getenv("JCRYPTO_INI_PATH"))
        						   .orElse(System.getProperty("JCRYPTO_INI_PATH"));
        
        // If the environment variable is not set, set JCRYPTO_INI_PATH to the default value
        if(JCRYPTO_INI_PATH == null || JCRYPTO_INI_PATH.trim().isEmpty()) {
        	JCRYPTO_INI_PATH = "/app/config/config.ini";
        	Log.warning("Environment variable JCRYPTO_INI_PATH not defined. Setting INI path to default value: " + JCRYPTO_INI_PATH);
        }
        
        // This environment variable could be omitted or overridden.
        // If so, the requests must contain JSON `"organization: "your_org_name"` in the body request
        final String JCRYPTO_DEFAULT_ORGANIZATION = Optional.ofNullable(System.getenv("JCRYPTO_DEFAULT_ORGANIZATION"))
        													.orElse(System.getProperty("JCRYPTO_DEFAULT_ORGANIZATION")); 
        
        // Return a new instance of Authenticate class with the default organization and the path to the INI file
        return new Authenticate(JCRYPTO_DEFAULT_ORGANIZATION, JCRYPTO_INI_PATH);
    }
    
}

