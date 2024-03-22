package com.thalesdemo.safenet.token.api.util;

import javax.xml.XMLConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;

public class SoapMessageUtil {

    // Private constructor to prevent instantiation
    private SoapMessageUtil() {
        throw new UnsupportedOperationException("SOAPMessageUtil is a utility class and cannot be instantiated");
    }

    /**
     * Convert the content of the given SOAPMessage to a String.
     *
     * @param message the SOAPMessage to be printed.
     * 
     * @return the content of the SOAPMessage as a String.
     */
    public static String soapMessageToString(SOAPMessage message) {
        try {
            // Create a new TransformerFactory to transform the SOAPMessage to a String
            // and set the security features to prevent XXE attacks
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            Transformer transformer = transformerFactory.newTransformer();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(message.getSOAPPart().getContent(), new StreamResult(os));
            return os.toString(); // return requestContent

        } catch (Exception e) {
            e.printStackTrace(); // You can handle this exception more gracefully if needed
        }
        return null;
    }

}