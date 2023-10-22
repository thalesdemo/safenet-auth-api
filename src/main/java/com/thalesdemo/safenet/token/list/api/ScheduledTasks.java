package com.thalesdemo.safenet.token.list.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

@Service
public class ScheduledTasks {

    private static final Logger logger = Logger.getLogger(ScheduledTasks.class.getName());

    @Autowired
    private SOAPClientService clientService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenDataService tokenDataService;

    @Value("${bsidca.scheduling.ping-interval}")
    private int pingIntervalInSeconds;

    @Value("${bsidca.scheduling.ping-retries}")
    private int pingRetries;

    @Value("${bsidca.scheduling.ping-timeout}")
    private int pingTimeoutInSeconds;

    @PostConstruct
    public void init() throws Exception {
        // Initialization logic that should run after pingConnection
        // This method will be called after bean construction and pingConnection
        logger.info("Initialization logic in @PostConstruct. Connecting to BSIDCA...");
        clientService.connect();
        connectionKeepAlive();

        // Check if storageFile exists
        File storage = new File(configService.getTokenStorageFile());

        if (!storage.exists() || storage.isDirectory()) {
            logger.info("Storage file doesn't exist. Fetching inventory...");
            getInventory();
        } else {
            logger.info("Storage file already exists. Skipping inventory fetch in startup initialization.");

            try {
                List<TokenDataDTO> tokens = tokenDataService.loadTokensFromFile(configService.getTokenStorageFile());

                // Process the loaded tokens as needed
                for (TokenDataDTO token : tokens) {
                    tokenDataService.addTokenType(token.getSerialNumber(), token.getType());
                }

                System.out.println("Loaded tokens from storage file: " + tokens.size());
                System.out.println("tokenData: " + tokens);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error loading tokens from storage file.", e);
                // Handle the IOException as needed (e.g., logging, fallback behavior)
            }
        }
    }

    @Scheduled(fixedRateString = "#{${bsidca.scheduling.ping-interval} * 1000}", initialDelayString = "#{${bsidca.scheduling.ping-interval} * 1000}")
    public void connectionKeepAlive() {
        try {
            for (int i = 0; i < pingRetries; i++) {
                boolean resultPing = clientService.pingConnection(pingTimeoutInSeconds);

                logger.log(Level.INFO, "Ping the connection every {0} seconds. Result = {1}",
                        new Object[] { pingIntervalInSeconds, resultPing });

                if (resultPing) {
                    // Ping successful
                    break;
                } else {
                    logger.log(Level.WARNING, "Ping failed on attempt {0}. Trying to reconnect...", i + 1);
                    clientService.connect();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during connection keep-alive process.", e);
        }
    }

    @Scheduled(cron = "${bsidca.scheduling.cron-inventory}")
    public void getInventory() {
        logger.info("Getting the inventory on the schedule defined in application.properties.");

        // Extract the parameters as required, for example:
        String state = null; // Or a default value if necessary
        String type = null; // Or a default value if necessary
        String serial = null; // Or a default value if necessary
        String container = null; // Or a default value if necessary
        String organization = null; // Or a default value if necessary

        try {
            if (organization == null) {
                organization = configService.getOrganization();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting organization.", e);
            // Consider re-throwing or handling the exception further as per your use-case
        }

        List<TokenDataDTO> tokens = tokenService.getAllTokens(state, type, serial, container, organization);

        tokenService.storeTokens(tokens);

        // Store token types in TokenDataService
        for (TokenDataDTO token : tokens) {
            tokenDataService.addTokenType(token.getSerialNumber(), token.getType());
        }
    }

}
