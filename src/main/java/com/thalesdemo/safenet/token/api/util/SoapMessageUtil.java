package com.thalesdemo.safenet.token.api.util;

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
     * Prints the content of the given SOAPMessage to the screen.
     *
     * @param message the SOAPMessage to be printed.
     */
    public static void printSoapMessageToScreen(SOAPMessage message) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(message.getSOAPPart().getContent(), new StreamResult(os));
            String requestContent = os.toString();

            System.out.println(requestContent);
        } catch (Exception e) {
            e.printStackTrace(); // You can handle this exception more gracefully if needed
        }
    }

    /**
     * Prints the content of the given SOAPMessage to the screen.
     *
     * @param message the SOAPMessage to be printed.
     */
    public static String soapMessageToString(SOAPMessage message) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(message.getSOAPPart().getContent(), new StreamResult(os));
            String requestContent = os.toString();

            return requestContent;
        } catch (Exception e) {
            e.printStackTrace(); // You can handle this exception more gracefully if needed
        }
        return null;
    }

}