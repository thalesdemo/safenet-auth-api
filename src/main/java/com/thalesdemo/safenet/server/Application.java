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
 * Main class that starts the Spring Boot application.
 * This class uses Spring Boot annotations to enable auto-configuration, component scanning, and more.
 * The main method of this class is responsible for starting the Spring Boot application.
 * To run this class, use the command "mvn spring-boot:run" or execute the main method in your IDE.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.thalesdemo.safenet.auth.api.exception.IniFilePathNotFoundException;
import com.thalesdemo.safenet.server.security.WebSslPasswordInitializer;

@SpringBootApplication
@ComponentScan(basePackages = "com.thalesdemo.safenet")
@EnableConfigurationProperties
@EnableScheduling
public class Application {
	public static void main(String[] args) {
		try {
			SpringApplication application = new SpringApplication(Application.class);
			application.addInitializers(new WebSslPasswordInitializer());
			application.run(args);
		} catch (IniFilePathNotFoundException e) {
			// System.err.println(e.getMessage());
			System.exit(1); // Exit with a non-zero status to indicate an error.
		}
	}
}