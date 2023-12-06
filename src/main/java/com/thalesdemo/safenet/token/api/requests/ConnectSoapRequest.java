package com.thalesdemo.safenet.token.api.requests;

import javax.xml.namespace.QName;
import javax.xml.soap.*;

public class ConnectSoapRequest {
    private ConnectSoapRequest() {
        throw new IllegalStateException("Utility class");
    }

    public static SOAPMessage createConnectRequest(char[] operatorEmail, char[] otp, char[] validationCode)
            throws SOAPException {

        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("soap12", "http://www.w3.org/2003/05/soap-envelope");

        SOAPBody body = envelope.getBody();

        if (validationCode == null) {
            validationCode = new char[0];
        }

        // Create the "Connect" element and its child elements
        SOAPElement connectElement = body.addChildElement("Connect", "", "http://www.cryptocard.com/blackshield/");
        SOAPElement operatorEmailElement = connectElement.addChildElement("OperatorEmail");
        operatorEmailElement.addTextNode(new String(operatorEmail));
        SOAPElement otpElement = connectElement.addChildElement("OTP");
        otpElement.addTextNode(new String(otp));
        SOAPElement validationCodeElement = connectElement.addChildElement("validationCode");
        validationCodeElement.addTextNode(new String(validationCode));

        return message;
    }

}
