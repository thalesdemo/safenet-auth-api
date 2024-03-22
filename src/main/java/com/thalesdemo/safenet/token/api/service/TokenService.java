package com.thalesdemo.safenet.token.api.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.thalesdemo.safenet.token.api.AuthenticatorResponses;
import com.thalesdemo.safenet.token.api.dto.GetTotalTokensResponseDTO;
import com.thalesdemo.safenet.token.api.dto.TokenDTO;
import com.thalesdemo.safenet.token.api.dto.TokenDataDTO;
import com.thalesdemo.safenet.token.api.request.GetTokensSoapRequest;
import com.thalesdemo.safenet.token.api.request.GetTotalTokensSoapRequest;
import com.thalesdemo.safenet.token.api.util.TokenStorage;

import okhttp3.Response;

@Service
public class TokenService {

    @Value("${safenet.bsidca.token.storage-file}")
    private String storagePath;

    @Value("${safenet.bsidca.connection-page-size:100}")
    private int pageSize;

    private SOAPClientService soapClientService;

    private ConfigService configService;

    private static final Logger logger = Logger.getLogger(TokenService.class.getName());

    public TokenService(SOAPClientService soapClientService, ConfigService configService) {
        this.soapClientService = soapClientService;
        this.configService = configService;
    }

    public List<TokenDataDTO> getTokens(String state, String type, String serial, String container, String organization,
            int startRecord, int pageSize) {
        try {
            SOAPMessage request = GetTokensSoapRequest.createGetTokensRequest(state, type, serial, container,
                    organization, startRecord, pageSize);
            Response response = soapClientService.sendSOAPRequestOkHttp(request);

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

    private List<TokenDataDTO> processSOAPResponse(Response response) throws Exception {
        String responseBody = response.body().string();
        return extractTokensFromResponseBody(responseBody);
    }

    private List<TokenDataDTO> extractTokensFromResponseBody(String xmlResponse) {
        List<TokenDataDTO> tokensList = new ArrayList<>();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

            // Extract token data from the response
            NodeList nodeList = doc.getElementsByTagName("Named_Tokens_Table");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                TokenDataDTO tokenData = new TokenDataDTO();

                tokenData.setSerialNumber(element.getElementsByTagName("serialnumber").item(0).getTextContent().trim());
                tokenData.setType(element.getElementsByTagName("type").item(0).getTextContent().trim());

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

            Response response = soapClientService.sendSOAPRequestOkHttp(request);
            String responseBody = response.body().string();
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
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
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

    public List<TokenDTO> getTokensByOwner(String userName, Optional<String> organization, int timeout)
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

        return soapClientService.getTokensByOwner(userName, org, timeout);
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