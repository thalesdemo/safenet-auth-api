package com.thalesdemo.safenet.token.api.requests;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class GetTokenSoapRequest {

    private GetTokenSoapRequest() {
        throw new IllegalStateException("Utility class");
    }

    public static SOAPMessage createGetTokenRequest(String serial, String organization) throws SOAPException {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();

        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("soap12", "http://www.w3.org/2003/05/soap-envelope");

        SOAPBody body = envelope.getBody();

        // Create the "GetToken" element and its child elements
        SOAPElement getTokenElement = body.addChildElement("GetToken", "", "http://www.cryptocard.com/blackshield/");

        if (serial != null) {
            SOAPElement serialElement = getTokenElement.addChildElement("serial");
            serialElement.addTextNode(serial);
        }

        if (organization != null) {
            SOAPElement organizationElement = getTokenElement.addChildElement("organization");
            organizationElement.addTextNode(organization);
        }

        return message;
    }
}
