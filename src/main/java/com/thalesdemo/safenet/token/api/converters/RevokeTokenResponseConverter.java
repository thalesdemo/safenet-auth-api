package com.thalesdemo.safenet.token.api.converters;

import org.springframework.stereotype.Component;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.thalesdemo.safenet.token.api.dto.TokenRevokeResponse;

import java.io.StringReader;

@Component
public class RevokeTokenResponseConverter implements SoapResponseConverter {

    @Override
    public TokenRevokeResponse convertToDTO(String soapResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(soapResponse)));

            NodeList resultNodes = document.getElementsByTagName("RevokeTokenResult");
            String revokeResult = resultNodes.getLength() > 0 ? resultNodes.item(0).getTextContent() : null;

            return new TokenRevokeResponse("Token revocation processed", revokeResult);

        } catch (Exception e) {
            e.printStackTrace();
            // Handle or log the exception as needed
            return new TokenRevokeResponse("Failed to process SOAP response", null);
        }
    }
}
