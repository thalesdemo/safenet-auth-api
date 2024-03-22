package com.thalesdemo.safenet.token.api.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.thalesdemo.safenet.token.api.dto.UserDTO;

public class UserResponseParser {

    private UserResponseParser() {
        throw new IllegalStateException("Utility class");
    }

    public static UserDTO extractUserDetailsFromResponse(String xmlResponse) {
        UserDTO userDetails = new UserDTO();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setNamespaceAware(true); // Enable namespace awareness
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

            // Adjust to properly handle namespaces
            NodeList resultNodes = doc.getElementsByTagNameNS("*", "GetUserResult");
            if (resultNodes.getLength() > 0) {
                Element resultElement = (Element) resultNodes.item(0);

                // Directly accessing without considering namespaces
                userDetails.setUserName(getElementTextContent(resultElement, "UserName"));
                userDetails.setEmail(getElementTextContent(resultElement, "Email"));
                userDetails.setMobile(getElementTextContent(resultElement, "Mobile"));
                // Add similar lines for other fields

                // Example for processing FirstName (assuming it's directly under GetUserResult)
                userDetails.setFirstName(getElementTextContent(resultElement, "FirstName"));

                // Handling for groups and custom attributes should adjust for their structure
                // Process groups (omitted here for brevity)
                // Process custom attributes similarly
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Proper error handling
        }

        return userDetails;
    }

    private static String getElementTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tagName); // Adjusted for namespace
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null; // Changed to return null to clearly indicate absence
    }
}