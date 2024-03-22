package com.thalesdemo.safenet.token.api.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.thalesdemo.safenet.token.api.dto.TokenDetailsDTO;

public class TokenDetailsParser {

    private TokenDetailsParser() {
        // Prevent instantiation, throw Utility class message
        throw new IllegalStateException("Utility class");
    }

    public static TokenDetailsDTO extractTokenDetailsFromResponseBody(String xmlResponse) {
        TokenDetailsDTO tokenDetails = new TokenDetailsDTO();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

            // Assuming the root element contains the token details directly
            Element rootElement = doc.getDocumentElement();

            tokenDetails.setState(getElementTextContent(rootElement, "State"));
            tokenDetails.setUnlockTime(OffsetDateTime.parse(getElementTextContent(rootElement, "UnlockTime")));

            tokenDetails.setOTPTTL(Integer.parseInt(getElementTextContent(rootElement, "OTPTTL")));
            tokenDetails.setOtpLength(Integer.parseInt(getElementTextContent(rootElement, "OtpLength")));
            tokenDetails.setActivationCount(Integer.parseInt(getElementTextContent(rootElement, "ActivationCount")));
            tokenDetails.setAuthAttempts(Integer.parseInt(getElementTextContent(rootElement, "AuthAttempts")));
            tokenDetails.setLastChallengeDate(
                    OffsetDateTime.parse(getElementTextContent(rootElement, "LastChallengeDate")));
            tokenDetails.setTimeBased(Boolean.parseBoolean(getElementTextContent(rootElement, "IsTimeBased")));
            tokenDetails.setTimeInterval(Integer.parseInt(getElementTextContent(rootElement, "TimeInterval")));
            tokenDetails.setDeviceName(getElementTextContent(rootElement, "DeviceName"));
            tokenDetails.setLastAuthDate(OffsetDateTime.parse(getElementTextContent(rootElement, "LastAuthDate")));
            tokenDetails
                    .setLastSuccessDate(OffsetDateTime.parse(getElementTextContent(rootElement, "LastSuccessDate")));

        } catch (Exception e) {
            e.printStackTrace();
            // Handle the error properly
        }

        return tokenDetails;
    }

    private static String getElementTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return ""; // Or handle absence of tag differently
    }
}
