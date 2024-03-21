package com.thalesdemo.safenet.token.api.service;

import java.io.StringReader;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.thalesdemo.safenet.token.api.AuthenticationOptions;
import com.thalesdemo.safenet.token.api.SOAPConfiguration;
import com.thalesdemo.safenet.token.api.dto.TokenDTO;
import com.thalesdemo.safenet.token.api.dto.TokenDetailsDTO;
import com.thalesdemo.safenet.token.api.dto.UserDTO;
import com.thalesdemo.safenet.token.api.exception.ApiException;
import com.thalesdemo.safenet.token.api.exception.ConnectionException;
import com.thalesdemo.safenet.token.api.request.ConnectSoapRequest;
import com.thalesdemo.safenet.token.api.request.GetTokenSoapRequest;
import com.thalesdemo.safenet.token.api.request.GetUserSoapRequest;
import com.thalesdemo.safenet.token.api.util.OkHttpUtil;
import com.thalesdemo.safenet.token.api.util.SoapMessageUtil;
import com.thalesdemo.safenet.token.api.util.TokenDetailsParser;
import com.thalesdemo.safenet.token.api.util.TokenUtils;
import com.thalesdemo.safenet.token.api.util.UserResponseParser;

import okhttp3.Response;
import okhttp3.ResponseBody;

@Service
public class SOAPClientService {

    private static final Logger logger = Logger.getLogger(SOAPClientService.class.getName());

    @Autowired
    private ConfigService configService;

    @Autowired
    private TokenDataService tokenDataService;

    @Autowired
    private AuthenticationOptions authenticationOptions;

    private SOAPConfiguration configuration;

    public SOAPClientService() {
        this.configuration = null;
    }

    protected String connect() throws Exception {
        char[] decryptedEmail = configService.getDecryptedEmailValue();
        char[] decryptedPassword = configService.getDecryptedPasswordValue();
        String baseUrl = configService.getBsidcaBaseUrl();

        if (configuration == null) {
            configuration = new SOAPConfiguration(baseUrl);
        }

        try {
            SOAPMessage connectRequest = ConnectSoapRequest.createConnectRequest(decryptedEmail, decryptedPassword,
                    null);
            logger.log(Level.FINE, "connectRequest: {0}", SoapMessageUtil.soapMessageToString(connectRequest));

            List<String> cookies = sendConnectSOAPRequestOkHttp(connectRequest);
            configService.clearSensitiveData(decryptedEmail);
            configService.clearSensitiveData(decryptedPassword);

            logger.finest("Setting cookies in SOAPConfiguration to: " + cookies);
            configuration.setCookies(cookies);

            return "Connected with cookies: " + cookies + "\n";

        } catch (ApiException ae) {
            logger.severe("API error: " + ae.getMessage());
            return ae.getMessage();
        } catch (ConnectionException ce) {
            logger.severe("Connection error: " + ce.getMessage());
            return "Connection failed due to: " + ce.getMessage();
        } catch (Exception e) {
            logger.severe("Unexpected error during connection: " + e.getMessage());
            return "Unexpected error during connection: " + e.getMessage();
        }
    }

    public SOAPConfiguration getConfiguration() throws Exception {
        if (configuration == null || configuration.getCookies() == null || configuration.getCookies().isEmpty()) {
            // Logic to reconnect
            connect();
        }
        return configuration;
    }

    public void setConfiguration(SOAPConfiguration configuration) {
        this.configuration = configuration;
    }

    protected Response sendSOAPRequestOkHttp(SOAPMessage request) throws Exception {
        String soapMessageString = SoapMessageUtil.soapMessageToString(request);
        String contentType = "application/soap+xml; charset=utf-8";

        // Get timeout from config service
        Integer timeout = configService.getDefaultHttpRequestTimeout();

        // Get the base URL from your configuration
        String url = configuration.getBaseUrl();

        // Get cookies from your configuration
        List<String> cookies = configuration.getCookies();

        // Use the refactored sendPostRequest method from OkHttpUtil
        return OkHttpUtil.sendPostRequest(url, soapMessageString, cookies, contentType, timeout);
    }

    private List<String> sendConnectSOAPRequestOkHttp(SOAPMessage request) throws Exception {
        // Clear any existing cookies
        configuration.clearCookies();

        // Send the SOAP request
        Response response = sendSOAPRequestOkHttp(request);

        // Extract the status code and response body
        int statusCode = response.code();
        ResponseBody responseBody = response.body();
        String responseBodyString = responseBody != null ? responseBody.string() : "";

        logger.log(Level.INFO, "SOAP Connect() Status Code Response: {0}", statusCode);
        logger.log(Level.INFO, "SOAP Connect() Response Body:\n{0}", responseBodyString);

        try {
            if (statusCode == 200 && responseBodyString.contains("AUTH_SUCCESS")) {
                // Extract and return cookies if the response is successful
                List<String> cookiesResponse = extractCookiesFromResponse(response);
                if (cookiesResponse.isEmpty()) {
                    // If no new cookies were found, use the existing ones
                    return configuration.getCookies();
                }
                return cookiesResponse;
            } else if (responseBodyString.contains("AUTH_FAILURE")) {
                throw new ApiException("Authentication failed");
            } else {
                throw new ApiException("Unexpected response content: " + responseBodyString);
            }
        } finally {
            // Ensure the response body is closed to release resources
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }

    // This method needs to be implemented to extract cookies from the OkHttp
    // Response
    private List<String> extractCookiesFromResponse(Response response) {
        // Get all 'Set-Cookie' headers from the response
        List<String> setCookieHeaders = response.headers("Set-Cookie");

        List<String> cookies = new ArrayList<>();

        for (String header : setCookieHeaders) {
            if (header != null && !header.isEmpty()) {
                cookies.add(header);
            }
        }

        return cookies;
    }

    public boolean pingConnection() {
        return pingConnection(10, false);
    }

    public boolean pingConnection(boolean useGet) {
        return pingConnection(10, useGet);
    }

    public boolean pingConnection(int timeout) {
        return pingConnection(timeout, true);
    }

    public boolean pingConnection(int timeout, boolean useGet) {
        Response response = null;

        try {
            // Check for null values and if connected.
            if (configuration == null || configuration.getBaseUrl() == null || !configuration.isConnected()) {
                logger.log(Level.WARNING, "Not connected to BSIDCA server.");
                return false;
            }

            if (useGet) {
                response = OkHttpUtil.sendGetRequest(
                        configuration.getBaseUrl() + "/PingConnection",
                        configuration.getCookies(),
                        timeout);
            } else {
                response = OkHttpUtil.sendPostRequest(
                        configuration.getBaseUrl() + "/PingConnection",
                        null, configuration.getCookies(),
                        "application/x-www-form-urlencoded",
                        timeout);
            }

            // Check the HTTP status code
            int statusCode = response.code();
            if (statusCode >= 200 && statusCode < 300) {
                // Parse the XML content from the response entity
                String responseContent = response.body().string(); // EntityUtils.toString(response.getEntity(),
                                                                   // "UTF-8");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(responseContent));
                Document doc = builder.parse(is);

                // Extract the boolean value from the XML
                String booleanValue = doc.getDocumentElement().getTextContent();

                // Close the response
                response.close();

                // Return true only if the extracted boolean value is "true"
                return "true".equalsIgnoreCase(booleanValue);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // In case of any exception, ensure the response is closed
            try {
                if (response != null)
                    response.close();
            } catch (Exception closeException) {
                closeException.printStackTrace();
            }
        }
        return false;

    }

    private List<String> parseXMLResponse(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlResponse));
        Document document = builder.parse(is);

        List<String> tokenStrings = new ArrayList<>();
        NodeList stringNodes = document.getElementsByTagName("string");

        for (int i = 0; i < stringNodes.getLength(); i++) {
            Element stringElement = (Element) stringNodes.item(i);
            String tokenString = stringElement.getTextContent();
            tokenStrings.add(tokenString);
        }

        return tokenStrings;
    }

    public List<TokenDTO> getTokensByOwner(String userName, String organization, int timeout) throws Exception {
        List<String> tokenSerials = fetchTokenSerialsByOwner(userName, organization, timeout);
        return mapSerialsToTokenDTOs(tokenSerials, userName, organization);
    }

    public List<String> getOptionsListByOwner(String userName, String organization, int timeout) throws Exception {
        List<String> tokenSerials = fetchTokenSerialsByOwner(userName, organization, timeout);
        return extractOptionsFromSerials(tokenSerials);
    }

    private List<String> fetchTokenSerialsByOwner(String userName, String organization, int timeout) throws Exception {
        Response response = null;
        try {

            String url = configuration.getBaseUrl() + "/GetTokensByOwner";

            // Construct the request body for x-www-form-urlencoded
            String requestBody = "userName=" + URLEncoder.encode(userName, "UTF-8")
                    + "&organization=" + URLEncoder.encode(organization, "UTF-8");

            response = OkHttpUtil.sendPostRequest(url, requestBody, configuration.getCookies(),
                    "application/x-www-form-urlencoded", timeout);

            if ((response.code() >= 200 || response.code() < 300) && response.body() != null) {
                String responseString = response.body().string();
                return parseXMLResponse(responseString);
            } else {
                response.close(); // close the response to release resources
                logger.fine("Closed response in else block");
                throw new RuntimeException("Empty response from server.");
            }
        } finally {
            if (response != null) {
                logger.fine("Closed response in finally block");
                response.close();
            }
        }
    }

    public List<String> extractOptionsFromSerials(List<String> tokenSerials) {
        // Map token serials to types
        List<String> tokenTypes = mapSerialsToTypes(tokenSerials);

        // Get unique presentation options
        Set<String> presentationOptions = new HashSet<>();
        for (String tokenType : tokenTypes) {
            List<String> options = authenticationOptions.getPresentationOptionsForTokenType(tokenType);
            presentationOptions.addAll(options);
        }

        return new ArrayList<>(presentationOptions);
    }

    private String normalizeTokenStateValue(String state) {

        // <State>BASE_INVENTORY or NOT_INITIALIZED or REVOKED or INITIALIZED or
        // CORRUPTED_INVENTORY or BASE_ALLOCATED or ALLOCATED or BASE_ASSIGNED or
        // MANUALLY_ASSIGNED or ENROLLED_PIN_CHANGE or ENROLLMENT_PENDING or BASE_ACTIVE
        // or ACTIVE_TOKEN or PIN_CHANGE or BASE_SUSPENDED or MANUAL_SUSPENSION or
        // RULE_SUSPENSION or BASE_LOCKED or SERVER_LOCK or USER_LOCK or PIN_CHANGE_LOCK
        // or CORRUPTED or BASE_LOST_FAILED or LOST_TOKEN or DAMAGED_TOKEN or
        // BASE_DELETED or MANUAL_REMOVE or AUTO_REMOVE or BASE_PURGED or
        // PURGED_TOKEN</State>

        String normalizedState = ""; // Default empty or perhaps consider a default state

        // Assuming 'state' is your input state variable
        switch (state.toUpperCase()) { // Convert to upper case for case-insensitive comparison

            // Active States
            case "BASE_ACTIVE":
            case "ACTIVE_TOKEN":
            case "BASE_ASSIGNED":
            case "MANUALLY_ASSIGNED":
                normalizedState = "active";
                break;

            // Suspended States
            case "BASE_SUSPENDED":
            case "MANUAL_SUSPENSION":
            case "RULE_SUSPENSION":
                normalizedState = "suspended";
                break;

            // Locked States
            case "BASE_LOCKED":
            case "SERVER_LOCK":
            case "USER_LOCK":
            case "PIN_CHANGE_LOCK":
                normalizedState = "locked";
                break;

            // PIN Change States
            case "ENROLLED_PIN_CHANGE":
            case "PIN_CHANGE":
                normalizedState = "pin_change";
                break;

            // Inactive States (assuming all others not explicitly ACTIVE, LOCKED,
            // SUSPENDED, or PIN_CHANGE fall into this)
            case "NOT_INITIALIZED":
            case "REVOKED":
            case "BASE_ALLOCATED":
            case "ALLOCATED":
            case "ENROLLMENT_PENDING":
            case "MANUAL_REMOVE":
            case "AUTO_REMOVE":
            case "BASE_DELETED":
            case "BASE_PURGED":
            case "PURGED_TOKEN":
                normalizedState = "inactive";
                break;

            // Failed tokens
            case "CORRUPTED_INVENTORY":
            case "CORRUPTED":
            case "BASE_LOST_FAILED":
            case "LOST_TOKEN":
            case "DAMAGED_TOKEN":
                normalizedState = "failed";
                break;

            default:
                logger.log(Level.WARNING, "Unknown state for token: {0}", state);
                normalizedState = state; // Return the original state if it's not recognized
                break;
        }

        return normalizedState;

    }

    // Used to check if the unlock time is in the past. In which case, the token
    // is unlock eligible and the unlock time is set to null along with the
    // state set to unlock_eligible
    private boolean isTimeInPast(OffsetDateTime time) {
        return time != null && time.isBefore(OffsetDateTime.now());
    }

    // unlockable, active, suspended, locked
    private List<TokenDTO> createTokenDTOs(String userName, String serial, String organization, String type,
            List<String> options) {
        List<TokenDTO> dtos = new ArrayList<>();

        List<String> specialOptions = Arrays.asList("sms", "voice", "email");

        // Get token details
        TokenDetailsDTO tokenDetails = this.getTokenDetails(serial, organization);

        String state = tokenDetails.getState();

        OffsetDateTime unlockTime = tokenDetails.getUnlockTime();

        if (this.isTimeInPast(unlockTime)) {
            unlockTime = null;
            if (state.equals("SERVER_LOCK"))
                state = "unlock_eligible";
        }
        state = normalizeTokenStateValue(state);

        Integer failedAttempts = tokenDetails.getAuthAttempts();

        OffsetDateTime lastAuthDate = tokenDetails.getLastAuthDate();
        OffsetDateTime lastSuccessDate = tokenDetails.getLastSuccessDate();

        // Step 1: Convert to nanoseconds (since the epoch) for precision.
        long nanosSinceEpoch = lastSuccessDate.toInstant().getEpochSecond() * 1_000_000_000L
                + lastSuccessDate.getNano();

        // Step 2: Calculate the target precision for hundredths of a second (10^7
        // nanoseconds).
        long targetPrecisionNanos = 10_000_000L; // 10^7 for hundredths of a second

        // Step 3: Perform rounding up to the nearest hundredth of a second.
        long remainder = nanosSinceEpoch % targetPrecisionNanos;
        long roundedNanosSinceEpoch = remainder == 0 ? nanosSinceEpoch
                : (nanosSinceEpoch - remainder + targetPrecisionNanos);

        // Step 4: Construct a new OffsetDateTime with the rounded time, ensuring the
        // same offset is preserved.
        OffsetDateTime roundedLastSuccessDate = OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(roundedNanosSinceEpoch / 1_000_000_000L,
                        roundedNanosSinceEpoch % 1_000_000_000L),
                lastSuccessDate.getOffset());

        if (lastAuthDate != null && roundedLastSuccessDate != null) {
            // Adjust lastSuccessDate to have the same timezone as lastAuthDate
            roundedLastSuccessDate = roundedLastSuccessDate.withOffsetSameInstant(lastAuthDate.getOffset());
        }

        if (Collections.disjoint(options, specialOptions)) {
            TokenDTO tokenDto = new TokenDTO();
            tokenDto.setSerial(serial);
            tokenDto.setType(type);
            tokenDto.setState(state);
            tokenDto.setUnlockTime(unlockTime);
            tokenDto.setFailedAttempts(failedAttempts);
            tokenDto.setOperatingSystem(tokenDetails.getDeviceName());
            tokenDto.setLastSuccessDate(roundedLastSuccessDate);

            if (options.contains("push")) {
                tokenDto.setPushCapable(true);
            } else if (type.equals("mobilepass")) {
                tokenDto.setPushCapable(false);
            }

            dtos.add(tokenDto);
            return dtos;
        } else {
            UserDTO user = this.getUserDetails(userName, organization);
            String defaultString = "----------";

            String phoneNumber = user != null && user.getMobile() != null ? user.getMobile() : defaultString;
            String email = user != null && user.getEmail() != null ? user.getEmail() : defaultString;

            if (options.contains("sms")) {
                TokenDTO smsDto = new TokenDTO();
                smsDto.setSerial(serial);
                smsDto.setType("sms");
                smsDto.setPhoneNumber(phoneNumber); // Ideally, fetch the actual number related to the token
                smsDto.setState(state);
                smsDto.setUnlockTime(unlockTime);
                smsDto.setFailedAttempts(failedAttempts);
                smsDto.setLastSuccessDate(roundedLastSuccessDate);
                dtos.add(smsDto);
            }

            if (options.contains("voice")) {
                TokenDTO voiceDto = new TokenDTO();
                voiceDto.setSerial(serial);
                voiceDto.setType("voice");
                voiceDto.setPhoneNumber(phoneNumber); // Fetch the phone number for VOICE, same or differently
                voiceDto.setState(state);
                voiceDto.setUnlockTime(unlockTime);
                voiceDto.setFailedAttempts(failedAttempts);
                voiceDto.setLastSuccessDate(roundedLastSuccessDate);
                dtos.add(voiceDto);
            }

            if (options.contains("email")) {
                TokenDTO emailDto = new TokenDTO();
                emailDto.setSerial(serial);
                emailDto.setType("email");
                emailDto.setEmail(email); // Fetch the actual email related to the token
                emailDto.setState(state);
                emailDto.setUnlockTime(unlockTime);
                emailDto.setFailedAttempts(failedAttempts);
                emailDto.setLastSuccessDate(roundedLastSuccessDate);
                dtos.add(emailDto);
            }
        }

        return dtos;
    }

    private List<TokenDTO> mapSerialsToTokenDTOs(List<String> tokenSerials, String userName, String organization) {

        // Create a list of TokenDTOs for each token serial
        List<TokenDTO> allTokenDTOs = new ArrayList<>();
        for (String serial : tokenSerials) {
            String tokenType = mapSerialToType(serial);
            List<String> options = authenticationOptions.getPresentationOptionsForTokenType(mapSerialToType(serial));
            allTokenDTOs.addAll(createTokenDTOs(userName, serial, organization, tokenType, options));
        }

        // Calculate the various counters for the options list
        int remainingAttempts = 0;
        int maxLockoutAttempts = configService.getUserLockoutPolicy();
        int overallFailedAttempts = TokenUtils.calculateOverallFailedAttempts(allTokenDTOs);

        // If any token is unlock eligible, reset the overall failed attempts and
        // remaining attempts
        if (TokenUtils.tokenEligibleForUnlock(allTokenDTOs)) {
            remainingAttempts = maxLockoutAttempts;
            overallFailedAttempts = 0;
        } else {
            remainingAttempts = maxLockoutAttempts - overallFailedAttempts;
            remainingAttempts = Math.max(0, remainingAttempts); // Ensure remainingAttempts is not negative
        }

        // if there are tokens, then add the options list
        if (!tokenSerials.isEmpty()) {
            // Create the options list and add it to the beginning of the token list
            TokenDTO optionsList = new TokenDTO();
            optionsList.setType("options");
            optionsList.setOptions(extractOptionsFromSerials(tokenSerials));
            optionsList.setMaxLockoutAttempts(maxLockoutAttempts);
            optionsList.setOverallFailedAttempts(overallFailedAttempts);
            optionsList.setRemainingAttempts(remainingAttempts);
            allTokenDTOs.add(0, optionsList);
        }

        return allTokenDTOs;
    }

    private List<String> mapSerialsToTypes(List<String> tokenSerials) {
        List<String> tokenTypes = new ArrayList<>();
        for (String serial : tokenSerials) {
            String type = tokenDataService.getTokenType(serial);
            tokenTypes.add(type);
        }
        return tokenTypes;
    }

    private String mapSerialToType(String serial) {
        return tokenDataService.getTokenType(serial);
    }

    public TokenDetailsDTO getTokenDetails(String serial, String organization) {
        try {
            SOAPMessage request = GetTokenSoapRequest.createGetTokenRequest(serial, organization);
            Response response = this.sendSOAPRequestOkHttp(request);

            // Handle the SOAP response, maybe extract tokens from it or handle errors.
            return processSOAPResponseForTokenDetails(response);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching token details", e);
        }
    }

    private TokenDetailsDTO processSOAPResponseForTokenDetails(Response response) throws Exception {
        String responseBody = response.body().string();
        return TokenDetailsParser.extractTokenDetailsFromResponseBody(responseBody);
    }

    public UserDTO getUserDetails(String userName, String organization) {
        try {
            SOAPMessage request = GetUserSoapRequest.createGetUserRequest(userName, organization);
            Response response = this.sendSOAPRequestOkHttp(request);

            // Handle the SOAP response, maybe extract tokens from it or handle errors.
            return processSOAPResponseForUserDetails(response);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching user details", e);
        }
    }

    private UserDTO processSOAPResponseForUserDetails(Response response) throws Exception {
        String responseBody = response.body().string();
        return UserResponseParser.extractUserDetailsFromResponse(responseBody);
    }
}
