package com.thalesdemo.safenet.token.api.converters;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalesdemo.safenet.token.api.dto.TokenProvisionResponse;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.xml.sax.InputSource;

@Component
public class ProvisionTokenResponseConverter implements SoapResponseConverter {

    @Override
    public Object convertToDTO(String soapResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(soapResponse)));

            NodeList resultNodes = document.getElementsByTagName("ProvisioningResult");
            String provisioningResult = resultNodes.getLength() > 0 ? resultNodes.item(0).getTextContent() : null;

            return new TokenProvisionResponse("Token provisioned successfully", provisioningResult);

        } catch (Exception e) {
            e.printStackTrace();
            // Handle or log the exception as needed
            return new TokenProvisionResponse("Failed to process SOAP response", null);
        }
    }

}
