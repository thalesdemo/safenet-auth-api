package com.thalesdemo.safenet.token.list.api.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpRequestUtil {

    private HttpRequestUtil() {
        throw new UnsupportedOperationException("HttpRequestUtil is a utility class and cannot be instantiated");
    }

    public static CloseableHttpResponse sendPostRequest(String url, String body, List<String> cookies,
            String contentType, Integer timeout) throws Exception {

        if (timeout == null) {
            timeout = 15; // 15 seconds
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout * 1000)
                .setConnectTimeout(timeout * 1000)
                .build();
        httpPost.setConfig(requestConfig);

        if (body != null && !body.isEmpty()) {
            StringEntity entity = new StringEntity(body);
            httpPost.setEntity(entity);
        }

        if (contentType != null) {
            httpPost.setHeader("Content-Type", contentType);
        }
        setCookies(httpPost, cookies);
        logRequest(httpPost); // Log the request
        return httpClient.execute(httpPost);
    }

    public static CloseableHttpResponse sendGetRequest(String url, List<String> cookies, Integer timeout)
            throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        setCookies(httpGet, cookies);

        logRequest(httpGet); // Log the request
        return httpClient.execute(httpGet);
    }

    private static void setCookies(HttpUriRequest request, List<String> cookies) {
        if (cookies != null && !cookies.isEmpty()) {
            for (String cookie : cookies) {
                request.addHeader("Cookie", cookie);
            }
        }
    }

    private static void logRequest(HttpRequest request) {
        System.out.println("----- Request Start -----");
        System.out.println(request.getRequestLine());

        // Print all headers
        for (Header header : request.getAllHeaders()) {
            System.out.println(header.getName() + ": " + header.getValue());
        }

        // Print the request body
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (entity != null && entity.isRepeatable()) {
                try {
                    System.out.println(EntityUtils.toString(entity));
                } catch (IOException e) {
                    System.out.println("Error reading request body: " + e.getMessage());
                }
            }
        }

        System.out.println("----- Request End -----");
    }
}
