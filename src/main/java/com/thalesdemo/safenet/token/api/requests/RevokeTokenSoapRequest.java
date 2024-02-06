package com.thalesdemo.safenet.token.api.requests;

import javax.xml.soap.*;

public class RevokeTokenSoapRequest {

    private RevokeTokenSoapRequest() {
        throw new IllegalStateException("Utility class");
    }

    public static SOAPMessage createRevokeTokenRequest(String serial, String userName, String comment,
            String revokeMode, boolean revokeStaticPassword,
            String organization) throws SOAPException {

        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();

        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("soap12", "http://www.w3.org/2003/05/soap-envelope");

        SOAPBody body = envelope.getBody();

        // Create the "RevokeToken" element and its child elements
        SOAPElement revokeTokenElement = body.addChildElement("RevokeToken", "",
                "http://www.cryptocard.com/blackshield/");

        // Add the "serial" child element
        SOAPElement serialElement = revokeTokenElement.addChildElement("serial");
        serialElement.addTextNode(serial);

        // Add the "userName" child element
        SOAPElement userNameElement = revokeTokenElement.addChildElement("userName");
        userNameElement.addTextNode(userName);

        // Add the "comment" child element
        SOAPElement commentElement = revokeTokenElement.addChildElement("comment");
        commentElement.addTextNode(comment);

        // Add the "revokeMode" child element
        SOAPElement revokeModeElement = revokeTokenElement.addChildElement("revokeMode");
        revokeModeElement.addTextNode(revokeMode);

        // Add the "revokeStaticPassword" child element
        SOAPElement revokeStaticPasswordElement = revokeTokenElement.addChildElement("revokeStaticPassword");
        revokeStaticPasswordElement.addTextNode(String.valueOf(revokeStaticPassword));

        // Add the "organization" child element
        SOAPElement organizationElement = revokeTokenElement.addChildElement("organization");
        organizationElement.addTextNode(organization);

        return message;
    }
}
