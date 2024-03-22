package com.thalesdemo.safenet.token.api.request;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class GetTokensSoapRequest {

    private GetTokensSoapRequest() {
        throw new IllegalStateException("Utility class");
    }

    public static SOAPMessage createGetTokensRequest(String state, String type, String serial, String container,
            String organization, int startRecord, int pageSize) throws SOAPException {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();

        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("soap12", "http://www.w3.org/2003/05/soap-envelope");

        SOAPBody body = envelope.getBody();

        // Create the "GetTokens" element and its child elements
        SOAPElement getTokensElement = body.addChildElement("GetTokens", "", "http://www.cryptocard.com/blackshield/");

        if (state != null) {
            SOAPElement stateElement = getTokensElement.addChildElement("state");
            stateElement.addTextNode(state);
        }

        if (type != null) {
            SOAPElement typeElement = getTokensElement.addChildElement("type");
            typeElement.addTextNode(type);
        }

        if (serial != null) {
            SOAPElement serialElement = getTokensElement.addChildElement("serial");
            serialElement.addTextNode(serial);
        }

        if (container != null) {
            SOAPElement containerElement = getTokensElement.addChildElement("container");
            containerElement.addTextNode(container);
        }

        if (organization != null) {
            SOAPElement organizationElement = getTokensElement.addChildElement("organization");
            organizationElement.addTextNode(organization);
        }

        SOAPElement startRecordElement = getTokensElement.addChildElement("startRecord");
        startRecordElement.addTextNode(String.valueOf(startRecord));

        SOAPElement pageSizeElement = getTokensElement.addChildElement("pageSize");
        pageSizeElement.addTextNode(String.valueOf(pageSize));

        return message;
    }
}
