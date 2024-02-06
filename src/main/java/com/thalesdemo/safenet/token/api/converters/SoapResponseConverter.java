package com.thalesdemo.safenet.token.api.converters;

public interface SoapResponseConverter {
    Object convertToDTO(String soapResponse) throws Exception;
}
