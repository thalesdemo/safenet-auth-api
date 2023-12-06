package com.thalesdemo.safenet.token.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thalesdemo.safenet.token.api.requests.GetTokensSoapRequest;
import com.thalesdemo.safenet.token.api.requests.GetTotalTokensSoapRequest;
import com.thalesdemo.safenet.token.api.util.HttpRequestUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.net.URLEncoder;

@Service
public class TokenService {

    @Value("${safenet.bsidca.token.storage-file}")
    private String storagePath;

    @Value("${safenet.bsidca.connection-page-size:100}")
    private int pageSize;

    @Autowired
    private SOAPClientService soapClientService;

    @Autowired
    private ConfigService configService;

    private static final Logger logger = Logger.getLogger(TokenService.class.getName());

    @Autowired
    private AuthenticationOptions authenticationOptions;

    // public String getPresentationTypeForTokenType(String tokenType) {
    // return authenticationOptions.getPresentationType(tokenType);
    // }

    public List<TokenDataDTO> getTokens(String state, String type, String serial, String container, String organization,
            int startRecord, int pageSize) {
        try {
            SOAPMessage request = GetTokensSoapRequest.createGetTokensRequest(state, type, serial, container,
                    organization, startRecord, pageSize);
            CloseableHttpResponse response = soapClientService.sendSOAPRequest(request);

            // Handle the SOAP response, maybe extract tokens from it or handle errors.
            return processSOAPResponse(response);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching tokens", e);
        }
    }

    public List<TokenDataDTO> getAllTokens(String state, String type, String serial,
            String container, String organization) {
        List<TokenDataDTO> allTokens = new ArrayList<>();

        try {
            int totalTokensCount = getTotalTokensCount(state, type, serial, container, organization);
            int totalPages = (int) Math.ceil((double) totalTokensCount / pageSize);

            for (int currentPage = 0; currentPage < totalPages; currentPage++) {
                int startRecord = currentPage * pageSize;

                List<TokenDataDTO> tokensOnCurrentPage = getTokens(state, type, serial, container, organization,
                        startRecord, pageSize);
                allTokens.addAll(tokensOnCurrentPage);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching all tokens.", e);
        }

        return allTokens;
    }

    private List<TokenDataDTO> processSOAPResponse(CloseableHttpResponse response) throws Exception {
        String responseBody = EntityUtils.toString(response.getEntity());
        return extractTokensFromResponseBody(responseBody);
    }
    // private List<TokenDataDTO> processSOAPResponse(CloseableHttpResponse
    // response) {
    // List<TokenDataDTO> extractedTokens = null;

    // try {
    // // Log headers
    // Header[] headers = response.getAllHeaders();
    // for (Header header : headers) {
    // logger.info(String.format("Header: %s - %s", header.getName(),
    // header.getValue()));
    // }

    // // Log body
    // String responseBody = EntityUtils.toString(response.getEntity());
    // logger.info(String.format("Response Body: %s", responseBody));

    // extractedTokens = extractTokensFromResponseBody(responseBody);

    // if (extractedTokens != null && !extractedTokens.isEmpty()) {
    // TokenStorage.storeTokens(extractedTokens, storagePath);
    // }

    // } catch (IOException e) {
    // logger.severe("Error processing SOAP response: " + e.getMessage());
    // }

    // return extractedTokens;
    // }

    private List<TokenDataDTO> extractTokensFromResponseBody(String xmlResponse) {
        List<TokenDataDTO> tokensList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

            // Extract token data from the response
            NodeList nodeList = doc.getElementsByTagName("Named_Tokens_Table");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                TokenDataDTO tokenData = new TokenDataDTO();

                tokenData.setSerialNumber(element.getElementsByTagName("serialnumber").item(0).getTextContent().trim());
                // tokenData.setState(element.getElementsByTagName("state").item(0).getTextContent().trim());
                // tokenData.setStateSetDate(element.getElementsByTagName("stateSetDate").item(0).getTextContent().trim());
                // tokenData.setOrgName(element.getElementsByTagName("orgName").item(0).getTextContent().trim());
                tokenData.setType(element.getElementsByTagName("type").item(0).getTextContent().trim());
                // tokenData.setContainer(element.getElementsByTagName("container").item(0).getTextContent().trim());
                // tokenData.setRented(element.getElementsByTagName("rented").item(0).getTextContent().trim());
                // tokenData.setHardwareInit(Boolean
                // .parseBoolean(element.getElementsByTagName("hardwareInit").item(0).getTextContent().trim()));
                // tokenData.setAssignable(Boolean
                // .parseBoolean(element.getElementsByTagName("assignable").item(0).getTextContent().trim()));
                // tokenData.setIce(element.getElementsByTagName("ice").item(0).getTextContent().trim());
                // tokenData.setStateInt(
                // Integer.parseInt(element.getElementsByTagName("stateInt").item(0).getTextContent().trim()));
                // tokenData.setTokenAllowed(
                // Integer.parseInt(element.getElementsByTagName("tokenAllowed").item(0).getTextContent().trim()));

                tokensList.add(tokenData);

            }

        } catch (Exception e) {
            logger.severe("Error extracting tokens from response: " + e.getMessage());
        }

        return tokensList;
    }

    public void storeTokens(List<TokenDataDTO> tokens) {
        try {
            TokenStorage.storeTokens(tokens, storagePath, configService.getEncryptionSecretKey());
        } catch (Exception e) {
            logger.severe("Error storing tokens: " + e.getMessage());
        }
    }

    public int getTotalTokensCount(String state, String type, String serial, String container, String organization)
            throws Exception {
        try {
            SOAPMessage request = GetTotalTokensSoapRequest.createGetTotalTokensRequest(state, type, serial, container,
                    organization);

            CloseableHttpResponse response = soapClientService.sendSOAPRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            logger.log(Level.FINE, "SOAP GetOrganizationTotalTokenCount Response Header:\n{0}", response);
            logger.log(Level.FINE, "SOAP GetOrganizationTotalTokenCount Response Body:{0}", responseBody);

            GetTotalTokensResponseDTO responseDto = extractTotalTokensFromResponseBody(responseBody);

            return responseDto.getTotalTokensCount();
        } catch (SOAPException e) {
            // Handle exception, maybe log it and throw a custom exception or return a
            // default value
            throw new RuntimeException("Failed to fetch total tokens count.", e);
        }
    }

    private GetTotalTokensResponseDTO extractTotalTokensFromResponseBody(String xmlResponse) {
        GetTotalTokensResponseDTO responseDto = new GetTotalTokensResponseDTO();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

            NodeList nodeList = doc.getElementsByTagName("GetTotalTokensResult");
            if (nodeList.getLength() > 0) {
                Element element = (Element) nodeList.item(0);
                int tokenCount = Integer.parseInt(element.getTextContent().trim());
                responseDto.setTotalTokensCount(tokenCount);
            }
        } catch (Exception e) {
            logger.severe("Error extracting total tokens count from response: " + e.getMessage());
        }

        return responseDto;
    }

    // TODO: Default to application.yaml for organization if not supplied, similar
    // to getOptionsListByOwner
    public TokenListDTO getTokensByOwner(String userName, String organization, int timeout) throws Exception {
        return soapClientService.getTokensByOwner(userName, organization, timeout);
    }

    public List<AuthenticatorResponses.AuthenticationOption> getOptionsListByOwner(String userName,
            Optional<String> organization, int timeout)
            throws Exception {
        String org = organization.orElseGet(() -> {
            try {
                return configService.getOrganization();
            } catch (Exception e) {
                // We re-throw as a runtime exception here, so that we're not changing the
                // method's exception contract.
                throw new RuntimeException("Failed to retrieve default organization", e);
            }
        });

        List<String> optionsStringList = soapClientService.getOptionsListByOwner(userName, org, timeout);
        return optionsStringList.stream()
                .map(AuthenticatorResponses::fromString)
                .collect(Collectors.toList());
    }

}
