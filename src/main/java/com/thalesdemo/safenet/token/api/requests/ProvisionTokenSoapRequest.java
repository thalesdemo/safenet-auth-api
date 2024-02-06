package com.thalesdemo.safenet.token.api.requests;

import javax.xml.soap.*;
import java.util.List;

public class ProvisionTokenSoapRequest {

    private ProvisionTokenSoapRequest() {
        throw new IllegalStateException("Utility class");
    }

    public static SOAPMessage createProvisionTokenRequest(
            List<String> userNames, String tokenClass, String description, String organization) throws SOAPException {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("soap12", "http://www.w3.org/2003/05/soap-envelope");

        SOAPBody body = envelope.getBody();
        SOAPElement provisionUsersElement = body.addChildElement("ProvisionUsers", "", "http://www.cryptocard.com/blackshield/");

        SOAPElement userNamesElement = provisionUsersElement.addChildElement("userNames");
        for (String userName : userNames) {
            SOAPElement stringElement = userNamesElement.addChildElement("string");
            stringElement.addTextNode(userName);
        }

        SOAPElement tokenClassElement = provisionUsersElement.addChildElement("tokenClass");
        tokenClassElement.addTextNode(tokenClass);

        SOAPElement descriptionElement = provisionUsersElement.addChildElement("description");
        descriptionElement.addTextNode(description);

        SOAPElement organizationElement = provisionUsersElement.addChildElement("organization");
        organizationElement.addTextNode(organization);

        return message;
    }

    public static SOAPMessage createProvisionGrIDsureTokenRequest(
            List<String> userNames, String description, String organization) throws SOAPException {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("soap12", "http://www.w3.org/2003/05/soap-envelope");

        SOAPBody body = envelope.getBody();
        SOAPElement provisionUsersGrIDsureTokensElement = body.addChildElement("ProvisionUsersGrIDsureTokens", "", "http://www.cryptocard.com/blackshield/");

        SOAPElement userNamesElement = provisionUsersGrIDsureTokensElement.addChildElement("userNames");
        for (String userName : userNames) {
            SOAPElement stringElement = userNamesElement.addChildElement("string");
            stringElement.addTextNode(userName);
        }

        SOAPElement descriptionElement = provisionUsersGrIDsureTokensElement.addChildElement("description");
        descriptionElement.addTextNode(description);

        SOAPElement organizationElement = provisionUsersGrIDsureTokensElement.addChildElement("organization");
        organizationElement.addTextNode(organization);

        return message;
    }
}
