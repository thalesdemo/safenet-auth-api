package com.thalesdemo.safenet.token.list.api.requests;

import javax.xml.soap.*;

public class GetTotalTokensSoapRequest {

    private GetTotalTokensSoapRequest() {
        throw new IllegalStateException("Utility class");
    }

    public static SOAPMessage createGetTotalTokensRequest(String state, String type, String serial, String container,
            String organization) throws SOAPException {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();

        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("soap12", "http://www.w3.org/2003/05/soap-envelope");

        SOAPBody body = envelope.getBody();

        // Create the "GetTotalTokens" element and its child elements
        SOAPElement getTotalTokensElement = body.addChildElement("GetTotalTokens", "",
                "http://www.cryptocard.com/blackshield/");

        if (state != null) {
            SOAPElement stateElement = getTotalTokensElement.addChildElement("state");
            stateElement.addTextNode(state);
        }

        if (type != null) {
            SOAPElement typeElement = getTotalTokensElement.addChildElement("type");
            typeElement.addTextNode(type);
        }

        if (serial != null) {
            SOAPElement serialElement = getTotalTokensElement.addChildElement("serial");
            serialElement.addTextNode(serial);
        }

        if (container != null) {
            SOAPElement containerElement = getTotalTokensElement.addChildElement("container");
            containerElement.addTextNode(container);
        }

        if (organization != null) {
            SOAPElement organizationElement = getTotalTokensElement.addChildElement("organization");
            organizationElement.addTextNode(organization);
        }

        return message;
    }
}
