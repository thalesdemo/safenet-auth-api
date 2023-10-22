package com.thalesdemo.safenet.auth.api;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class PropertiesInitializer {

    private String iniPath;

    /**
     * The logger instance used to log messages in this class.
     */

    private static final Logger Log = Logger.getLogger(AuthenticateConfig.class.getName());

    @Value("${safenet.token-validator.ini-path:}")
    private void setIniPath(String iniPath) {
        System.out.println("Setting iniPath: " + iniPath); // Just for visualization
        this.iniPath = iniPath;
        AuthenticateConfig.setIniPath(this.iniPath);
    }

    @PostConstruct
    private void postConstruct() {
        if (this.iniPath == null || this.iniPath.trim().isEmpty()) {
            Log.warning("safenet.token-validator.ini-path not found in properties/yml file.");
        }
    }
}
