package com.thalesdemo.safenet.token.api;

import java.util.ArrayList;
import java.util.List;

public class SOAPConfiguration {

    private String baseUrl;
    private List<String> cookies;

    public SOAPConfiguration(String baseUrl) {
        this.baseUrl = baseUrl;
        this.cookies = new ArrayList<>();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    public void clearCookies() {
        cookies.clear();
    }
    
    public boolean isConnected() {
        return !cookies.isEmpty();
    }

    @Override
    public String toString() {
        return "SOAPConfiguration{" +
                "baseUrl='" + baseUrl + '\'' +
                ", cookies=" + cookies +
                '}';
    }
}
