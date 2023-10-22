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
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.auth.api;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	/**
	 * This field stores the API key hash as a string. The value of this field is retrieved from an environment variable
	 * called "API_KEY_HASH" using the Spring @Value annotation.
	 */
	
	@Value("${safenet.api.key.hash}")
	private String API_KEY_HASH;

	
	/**
	 * This is the logger instance for the WebSecurityConfig class. The logger is initialized with the name of the class
     * for which it is used.
     */
	
	private static final Logger Log = Logger.getLogger(WebSecurityConfig.class.getName());
	
	
	/**
	 * A Spring bean that provides an instance of BCryptPasswordEncoder.
	 * BCryptPasswordEncoder is a password encoder that uses the BCrypt hash function to encode passwords.
	 */
	
	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	
	/**
	 * Returns an instance of the {@link ApiKeyAuthService} class that uses a {@link BCryptPasswordEncoder}
	 * and an API key hash as input. This bean is used to authenticate requests that require an API key for
	 * access to protected endpoints.
	 * @return An instance of the {@link ApiKeyAuthService} class.
	 */
    
	@Bean
    ApiKeyAuthService apiKeyAuthService() {
    	Log.fine("Entered Config apiKeyAuthService @Bean");
        return new ApiKeyAuthService(passwordEncoder(), API_KEY_HASH);
    }
    
	
	/**
	 * Configures the HTTP security for the application, setting session management policy to stateless and requiring
	 * an API key to access endpoints under the '/api' path.
	 * 
	 * If the API key is invalid, returns an HTTP status of 401 Unauthorized with a JSON error message.
	 * Disables CSRF protection, HTTP basic authentication, and form login, and sets the frame options for the headers
	 * to 'sameOrigin'.
	 * 
	 * @param http The HttpSecurity object to configure
	 * @throws Exception if an error occurs while configuring the HttpSecurity
	 */
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	   	
        http 
        	.sessionManagement()
        		.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/**")
                .access("@apiKeyAuthService.checkApiKey(request)")
                .and()
                .exceptionHandling()
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Invalid API key\"}");
                    })
                .and()       
	            .csrf().disable()
	            .httpBasic().disable()
	            .formLogin().disable()
	            .headers().frameOptions().sameOrigin();
    }

    
    /**
    * #TODO: To review all implementation available to suppress the following warning message at every start
    *        of the spring application:
    *     	 ```
    *      		WARN 8376 --- [  restartedMain] .s.s.UserDetailsServiceAutoConfiguration : 
    *      		Using generated security password: e7c68bd2-9005-476b-8af0-330fa34a2482
    *
    *      		This generated password is for development use only. Your security configuration must be updated
    *      	    before running your application in production.
    *    	 ```
    *
    * This method configures the authentication manager that will be used by authenticationManagerBean() below. 
    * You can add your own authentication manager by extending WebSecurityConfigurerAdapter and overriding this 
    * method, or by creating a bean of type AuthenticationManager.
    * 
    * @param authManager the AuthenticationManagerBuilder to use for configuring the authentication manager
    * @throws Exception if an error occurs while configuring the authentication manager
    */    
    
    @Override
    protected void configure(AuthenticationManagerBuilder authManager) throws Exception {
        // Code to configure your authentication manager
    	// ...
        // This method is used by the Bean authenticationManagerBean() 
    }

    /**
     * Bean configuration for the AuthenticationManager, required to prevent Spring Boot auto-configuration.
     *   
     * This bean is necessary to prevent the generated security password warning message on run:
     *   ```
     *   WARN 8376 --- [ restartedMain] .s.s.UserDetailsServiceAutoConfiguration :
     *   Using generated security password: e7c68bd2-9005-476b-8af0-330fa34a2482
     *   This generated password is for development use only. Your security configuration must be updated before
     *   running your application in production.
     *   ```
     *
     * This configuration ensures that the AuthenticationManager is correctly set up and overrides the Spring Boot
     * auto-configuration. It is used by the authenticationManager() method below.
     *
     * @return the authentication manager bean
     * @throws Exception if an exception occurs during the configuration process
     */
    
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // Required to prevent spring boot auto-configuration
        return super.authenticationManagerBean();
    }
      
}