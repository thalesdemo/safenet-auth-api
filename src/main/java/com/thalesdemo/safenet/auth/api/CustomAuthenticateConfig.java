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
 * This class is used to configure the custom authentication service
 * implementation. This (alternate) "custom" authentication implementation 
 * offers support for Push Authentication.
 * 
 * @see AuthenticateConfig
 * @see CustomAuthenticate
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.auth.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CustomAuthenticateConfig {
    
    /**
     * The logger for the CustomAuthenticateConfig class.
     */

    private static final Logger Log = Logger.getLogger(CustomAuthenticateConfig.class.getName());

    	
	/**
	 * This field stores the resource name as a string. The value of this field is retrieved from an environment variable
	 * called "RESOURCE_NAME" using the Spring @Value annotation.
     * 
     * This is the name of the resource that is being protected by the authentication service and which will be used in
     * the push authentication request message. 
     * 
     * For the resource name to be used in the push authentication request message, the TokenValidator URL MUST contain 
     * the orgCode query parameter. For example:
     *      http://cloud.us.safenetid.com/TokenValidator/TokenValidator.asmx?orgCode=123456
     * 
     * If the orgCode query parameter is not present in the TokenValidator URL, the resource name will be taken from
     * the authentication node configuration in the SafeNet Authentication Service console.
     * 
     * If the orgCode query parameter is present in the TokenValidator URL, and the resource name is not specified in the
     * environment variable RESOURCE_NAME, then the push authentication request message will not contain the resource 
     * name and show blank. For example:
     * 
     *     Push authentication request message without resource name:
     *          Login request from [missing resource name]
     * 
	 */
	
	@Value("${safenet.resource.name:}")
	private String RESOURCE_NAME;


    /**
     * This method returns the content of the agent key file as a string. The agent key file is specified in the
     * jcrypto.ini file. The path to the jcrypto.ini file is specified in the environment variable JCRYPTO_INI_PATH.
     * Retrieving the path is handled by the {@link AuthenticateConfig} class using the getAgentKeyPathFromJcryptoIni() method.
     * @return The content of the agent key file as a string.
     */

    public String readHostAgentKeyPath() {
        try {
            String content = Files.readString(Paths.get(AuthenticateConfig.getAgentKeyPathFromJcryptoIni()));
            return content;
        } catch (IOException ex) {
            return null;
        }
    }


    /**
     * This method adds a question mark to the end of the URL if it is missing.
     * This helps to avoid an error when the URL is used to create a {@link CustomAuthenticate} object leveraging the
     * the {@link TokenValidatorWrapperImpl} class.
     * @param url The URL to which the question mark will be added.
     * @return The URL with a question mark at the end.
     */
    
    public String addQueryStringIfMissing(String url) {

        if(url == null) return null;

        if (url.indexOf("?") == -1) {
            url += "?";
        }
        return url;
    }


    /**
     * This method returns an instance of the {@link CustomAuthenticate} class that is used to configure the authentication
     * service. The {@link CustomAuthenticate} class is an alternate TokenValidator proxy to circumvent limitations around the 
     * {@link Authenticate} class that using the the official SafeNet Authentication SDK, to enable use of modern
     * authentication methods such as Push OTP.
     * 
     * @return An instance of the {@link CustomAuthenticate} class.
     */

    @Bean
    CustomAuthenticate customAuthenticateCfgBean() {

        String primaryUrl = addQueryStringIfMissing(AuthenticateConfig.getPrimaryAuthUrlFromJcryptoIni());
        String secondaryUrl = addQueryStringIfMissing(AuthenticateConfig.getSecondaryAuthUrlFromJcryptoIni());
        Log.info("Primary Auth URL: " + primaryUrl);
        Log.info("Secondary Auth URL: " + secondaryUrl);

        return new CustomAuthenticate()
                .setPrimaryAuthUrl(primaryUrl)
                .setFailoverAuthUrl(secondaryUrl)
                .setAgentKey(readHostAgentKeyPath())
                .setResourceName(RESOURCE_NAME)
                .build(); // This is the line that needs to be added at the end to load the token validator wrapper
    }

}
