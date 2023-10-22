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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn("propertiesInitializer")
public class AuthenticateConfig {

    /**
     * The logger instance used to log messages in this class.
     */

    private static final Logger Log = Logger.getLogger(AuthenticateConfig.class.getName());

    private static String JCRYPTO_INI_PATH;

    public static void setIniPath(String path) {
        JCRYPTO_INI_PATH = path;
    }

    /**
     * The path to the INI file. If the JCRYPTO_INI_PATH environment variable is not
     * set,
     * return the default value '/app/config/config.ini' which is used by the Docker
     * container.
     * 
     * @return the path to the INI file
     */

    public static String getJcryptoIniPath() {
        if (JCRYPTO_INI_PATH == null || JCRYPTO_INI_PATH.trim().isEmpty()) {
            JCRYPTO_INI_PATH = System.getenv("SAFENET_INI_PATH");
        }

        if (JCRYPTO_INI_PATH == null || JCRYPTO_INI_PATH.trim().isEmpty()) {
            JCRYPTO_INI_PATH = System.getProperty("SAFENET_INI_PATH");
        }

        if (JCRYPTO_INI_PATH == null || JCRYPTO_INI_PATH.trim().isEmpty()) {
            JCRYPTO_INI_PATH = "/app/config/config.ini";
            Log.warning("JCRYPTO_INI_PATH not defined. Setting INI path to default value: " + JCRYPTO_INI_PATH);
        }

        return JCRYPTO_INI_PATH;

    }

    /**
     * Get the authentication URL from the INI file based on the configuration keys.
     * 
     * @param keyNameHttpProtocol    key name for the HTTP protocol
     * @param keyNameServerHost      key name for the server host
     * @param keyNameServerPort      key name for the server port
     * @param keyNameRelativeUrlPath key name for the relative URL path
     * @return the authentication URL
     */

    public static String getAuthUrlFromJcryptoIni(String keyNameHttpProtocol, String keyNameServerHost,
            String keyNameServerPort, String keyNameRelativeUrlPath) {
        String jCryptoIniPath = getJcryptoIniPath();

        // read the file and extract the auth url
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(jCryptoIniPath)) {
            props.load(in);
        } catch (IOException ex) {
            Log.severe("Could not read the INI file." + jCryptoIniPath);
            return null;
            // handle the exception, e.g. log an error message or throw a custom exception
        }

        // Retrieve the values of the properties
        String protocol = props.getProperty(keyNameHttpProtocol);
        String server = props.getProperty(keyNameServerHost);
        String port = props.getProperty(keyNameServerPort);
        String path = props.getProperty(keyNameRelativeUrlPath);

        // Form and return the absolute URL
        return String.format("%s://%s:%s%s", protocol, server, port, path);
    }

    /**
     * Get the agent key path from the INI file.
     * 
     * @return the agent key path
     */

    public static String getAgentKeyPathFromJcryptoIni() {
        String jCryptoIniPath = getJcryptoIniPath();

        // read the file and extract the agent key path
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(jCryptoIniPath)) {
            props.load(in);
        } catch (IOException ex) {
            Log.severe("Could not read the INI file." + jCryptoIniPath);
            return "/app/secret/agent.key"; // default value
        }

        // Retrieve the value of the property
        return props.getProperty("EncryptionKeyFile");
    }

    /**
     * Checks if the URL is valid.
     * 
     * @param url the URL to check
     * @return true if the URL is valid, false otherwise
     */

    public static boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            if (uri.getHost() == null) {
                return false;
            }
            return true;
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    /**
     * Get the primary authentication URL from the INI file.
     * 
     * @return the primary authentication URL
     */

    public static String getPrimaryAuthUrlFromJcryptoIni() {
        return getAuthUrlFromJcryptoIni("PrimaryProtocol", "PrimaryServer", "PrimaryServerPort",
                "PrimaryWebServiceRelativePath");
    }

    /**
     * Get the secondary authentication URL from the INI file.
     * 
     * @return the secondary authentication URL if it is defined in the INI file,
     *         otherwise the primary authentication URL
     */

    public static String getSecondaryAuthUrlFromJcryptoIni() {

        String secondaryUrl = getAuthUrlFromJcryptoIni("SecondaryProtocol", "SecondaryServer", "SecondaryServerPort",
                "SecondaryWebServiceRelativePath");

        if (secondaryUrl == null || secondaryUrl.trim().isEmpty() || !isValidUrl(secondaryUrl)) {
            Log.info("Secondary URL is not defined in the INI file. Mirroring the primary URL instead.");
            secondaryUrl = getPrimaryAuthUrlFromJcryptoIni();
        }
        return secondaryUrl;
    }

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

        String jCryptoIniPath = getJcryptoIniPath();

        // This environment variable could be omitted or overridden.
        // If so, the requests must contain JSON `"organization: "your_org_name"` in the
        // body request
        final String JCRYPTO_DEFAULT_ORGANIZATION = Optional.ofNullable(System.getenv("JCRYPTO_DEFAULT_ORGANIZATION"))
                .orElse(System.getProperty("JCRYPTO_DEFAULT_ORGANIZATION"));

        // Return a new instance of Authenticate class with the default organization and
        // the path to the INI file
        return new Authenticate(JCRYPTO_DEFAULT_ORGANIZATION, jCryptoIniPath);
    }

}
