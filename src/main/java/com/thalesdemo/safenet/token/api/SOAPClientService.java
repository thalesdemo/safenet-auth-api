package com.thalesdemo.safenet.token.api;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPMessage;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.thalesdemo.safenet.token.api.requests.ConnectSoapRequest;
import com.thalesdemo.safenet.token.api.util.HttpRequestUtil;
import com.thalesdemo.safenet.token.api.util.SoapMessageUtil;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

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

    private final CloseableHttpClient httpClient;

    public SOAPClientService() {
        this.configuration = null;
        this.httpClient = HttpClientPool.getHttpClient();
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
            logger.finest("connectRequest: " + SoapMessageUtil.soapMessageToString(connectRequest));

            List<String> cookies = sendConnectSOAPRequest(connectRequest);
            configService.clearSensitiveData(decryptedEmail);
            configService.clearSensitiveData(decryptedPassword);

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

    private List<String> sendConnectSOAPRequest(SOAPMessage request) throws Exception {
        // Send the Connect SOAP request and extract the cookies
        HttpResponse response = sendSOAPRequest(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
        logger.log(Level.INFO, "SOAP BSIDCA Connect() Status Code Response: {0}", statusCode);
        logger.log(Level.INFO, "SOAP BSIDCA Connect() Response Body:\n{0}", responseBody);
        // Check if the response is successful (status code 200) and contains the
        // "Set-Cookie" headers
        if (statusCode == 200) {
            if (responseBody.contains("AUTH_SUCCESS")) {
                return extractCookiesFromResponse(response);
            } else if (responseBody.contains("AUTH_FAILURE")) {
                throw new ApiException("Authentication failed");
            } else {
                throw new ApiException("Unexpected response content: " + responseBody);
            }
        }

        // If status code is not 200, extract more details for the error message
        String reasonPhrase = response.getStatusLine().getReasonPhrase();

        String briefError = responseBody.length() > 400 ? responseBody.substring(0, 400) + "..." : responseBody;

        throw new ApiException(String.format("Failed to extract Connect() cookies. Status: %d %s. Response: %s",
                statusCode, reasonPhrase, briefError));
    }

    // protected CloseableHttpResponse sendSOAPRequest(SOAPMessage request) throws
    // Exception {
    // String soapMessageString = SoapMessageUtil.soapMessageToString(request);
    // return HttpRequestUtil.sendPostRequest(
    // configuration.getBaseUrl(),
    // soapMessageString,
    // configuration.getCookies(),
    // "application/soap+xml; charset=utf-8",
    // configService.getDefaultHttpRequestTimeout());
    // }

    protected CloseableHttpResponse sendSOAPRequest(SOAPMessage request) throws Exception {
        String soapMessageString = SoapMessageUtil.soapMessageToString(request);

        // Since you're using SOAP, your content type is fixed as "application/soap+xml;
        // charset=utf-8"
        String contentType = "application/soap+xml; charset=utf-8";

        // Get timeout from config service
        Integer timeout = configService.getDefaultHttpRequestTimeout();

        // Get the base URL from your configuration
        String url = configuration.getBaseUrl();

        // Get cookies from your configuration
        List<String> cookies = configuration.getCookies();

        // Use the refactored sendPostRequest method from HttpRequestUtil
        return HttpRequestUtil.sendPostRequest(url, soapMessageString, cookies, contentType, timeout);
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
        CloseableHttpResponse response = null;

        try {
            // Check for null values and if connected.
            if (configuration == null || configuration.getBaseUrl() == null || !configuration.isConnected()) {
                logger.log(Level.WARNING, "Not connected to BSIDCA server.");
                return false;
            }

            if (useGet) {
                response = HttpRequestUtil.sendGetRequest(
                        configuration.getBaseUrl() + "/PingConnection",
                        configuration.getCookies(),
                        timeout);
            } else {
                response = HttpRequestUtil.sendPostRequest(
                        configuration.getBaseUrl() + "/PingConnection",
                        null, configuration.getCookies(),
                        "application/x-www-form-urlencoded",
                        timeout);
            }

            // Check the HTTP status code. A status code in the 200s indicates success.
            int statusCode = response.getStatusLine().getStatusCode();
            return (statusCode >= 200 && statusCode < 300);

        } catch (Exception ex) {
            return false;
        } finally {
            // Close the response to free up resources.
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    // Handle the exception or log it.
                }
            }
        }
    }

    private List<String> extractCookiesFromResponse(HttpResponse response) {
        Header[] headers = response.getHeaders("Set-Cookie");
        List<String> cookies = new ArrayList<>();

        for (Header header : headers) {
            String cookieValue = header.getValue();
            if (cookieValue != null && !cookieValue.isEmpty()) {
                cookies.add(cookieValue);
            }
        }

        // Log the extracted cookies to your logger
        logger.log(Level.FINEST, "Extracted Cookies from Response: {0}", cookies);

        return cookies;
    }

    private List<String> parseXMLResponse(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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

    public TokenListDTO getTokensByOwner(String userName, String organization, int timeout) throws Exception {
        List<String> tokenSerials = fetchTokenSerialsByOwner(userName, organization, timeout);
        return mapSerialsToTypesAndOptions(tokenSerials, userName);
    }

    // public List<String> getTokensByOwnerWithDetails(String userName, String
    // organization, int timeout)
    // throws Exception {
    // List<String> tokenSerials = fetchTokenSerialsByOwner(userName, organization,
    // timeout);
    // return tokenSerials;
    // }

    public List<String> getOptionsListByOwner(String userName, String organization, int timeout) throws Exception {
        List<String> tokenSerials = fetchTokenSerialsByOwner(userName, organization, timeout);
        return extractOptionsFromSerials(tokenSerials);
    }

    private List<String> fetchTokenSerialsByOwner(String userName, String organization, int timeout) throws Exception {
        CloseableHttpResponse response = null;
        try {
            SOAPConfiguration configuration = getConfiguration();

            String url = configuration.getBaseUrl() + "/GetTokensByOwner";

            // Construct the request body for x-www-form-urlencoded
            String requestBody = "userName=" + URLEncoder.encode(userName, "UTF-8")
                    + "&organization=" + URLEncoder.encode(organization, "UTF-8");

            // Use the refactored sendPostRequest method from HttpRequestUtil with the
            // correct content type
            response = HttpRequestUtil.sendPostRequest(url, requestBody, configuration.getCookies(),
                    "application/x-www-form-urlencoded", timeout);

            if (response.getEntity() != null) {
                String responseString = EntityUtils.toString(response.getEntity());
                return parseXMLResponse(responseString);
            } else {
                throw new RuntimeException("Empty response from server.");
            }
        } finally {
            if (response != null) {
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

    private TokenListDTO mapSerialsToTypesAndOptions(List<String> tokenSerials, String userName) {
        // Create TokenListDTO from the parsed data
        TokenListDTO tokenListDTO = new TokenListDTO();
        tokenListDTO.setSerials(tokenSerials);
        tokenListDTO.setOwner(userName);

        // Map token serials to types
        List<String> tokenTypes = mapSerialsToTypes(tokenSerials);
        tokenListDTO.setTypes(tokenTypes);

        // Get unique presentation options
        Set<String> presentationOptions = new HashSet<>();
        for (String tokenType : tokenTypes) {
            List<String> options = authenticationOptions.getPresentationOptionsForTokenType(tokenType);
            presentationOptions.addAll(options);
        }
        tokenListDTO.setOptions(new ArrayList<>(presentationOptions));

        return tokenListDTO;
    }

    private List<String> mapSerialsToTypes(List<String> tokenSerials) {
        List<String> tokenTypes = new ArrayList<>();
        for (String serial : tokenSerials) {
            String type = tokenDataService.getTokenType(serial);
            tokenTypes.add(type);
        }
        return tokenTypes;
    }

}
