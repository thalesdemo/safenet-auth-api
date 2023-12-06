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

package com.thalesdemo.safenet.server.security;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * This field stores the API key hash as a string. The value of this field is
     * retrieved from an environment variable
     * called "API_KEY_HASH" using the Spring @Value annotation.
     */
    @Value("${safenet.api.key-hash}")
    private String API_KEY_HASH;

    /**
     * This is the logger instance for the WebSecurityConfig class. The logger is
     * initialized with the name of the class
     * for which it is used.
     */
    private static final Logger Log = Logger.getLogger(WebSecurityConfig.class.getName());

    /**
     * A Spring bean that provides an instance of BCryptPasswordEncoder.
     * BCryptPasswordEncoder is a password encoder that uses the BCrypt hash
     * function to encode passwords.
     */
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Returns an instance of the {@link ApiKeyAuthService} class that uses a
     * {@link BCryptPasswordEncoder}
     * and an API key hash as input. This bean is used to authenticate requests that
     * require an API key for
     * access to protected endpoints.
     * 
     * @return An instance of the {@link ApiKeyAuthService} class.
     */
    @Bean
    ApiKeyAuthService apiKeyAuthService() {
        Log.fine("Entered Config apiKeyAuthService @Bean");
        return new ApiKeyAuthService(passwordEncoder(), API_KEY_HASH);
    }

    /**
     * Bean configuration for the security filter chain. This chain applies the
     * security configuration to incoming requests.
     *
     * @return the security filter chain
     * @throws Exception if an exception occurs during the configuration process
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, IpAndHeaderBasedFilter ipAndHeaderBasedFilter)
            throws Exception {
        http
                .addFilterBefore(ipAndHeaderBasedFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .mvcMatchers("/api/**")
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
        return http.build();
    }

    @Bean
    public AuthenticationManager customAuthenticationManager() {
        return authentication -> {
            throw new UnsupportedOperationException("No custom authentication manager defined");
        };
    }

}