package com.thalesdemo.safenet.auth.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertiesInitializer {

    private String iniPath;

    /**
     * The logger instance used to log messages in this class.
     */

    private static final Logger logger = Logger.getLogger(PropertiesInitializer.class.getName());

    @Value("${safenet.token-validator.ini-path}")
    private void setIniPath(String iniPath) {
        logger.log(Level.INFO, "INI configuration path to: {0}", iniPath); // Just for visualization
        this.iniPath = iniPath;
        AuthenticateConfig.setIniPath(this.iniPath);
    }

    @PostConstruct
    private void postConstruct() {
        if (this.iniPath == null || this.iniPath.trim().isEmpty()) {
            logger.warning("Property safenet.token-validator.ini-path not found in application.yaml.");
        }
    }
}
