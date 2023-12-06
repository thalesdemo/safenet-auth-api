package com.thalesdemo.safenet.token.api.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.thalesdemo.safenet.token.api.HttpClientPool;
import com.thalesdemo.safenet.token.api.ScheduledTasks;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpUriRequest;

public class HttpRequestUtil {

    private static final Logger logger = Logger.getLogger(HttpRequestUtil.class.getName());

    private HttpRequestUtil() {
        throw new UnsupportedOperationException("HttpRequestUtil is a utility class and cannot be instantiated");
    }

    // public static CloseableHttpResponse sendPostRequest(String url, String body,
    // List<String> cookies,
    // String contentType, Integer timeout) throws Exception {

    // if (timeout == null) {
    // timeout = 15; // 15 seconds
    // }

    // CloseableHttpClient httpClient = HttpClients.createDefault();
    // HttpPost httpPost = new HttpPost(url);

    // RequestConfig requestConfig = RequestConfig.custom()
    // .setSocketTimeout(timeout * 1000)
    // .setConnectTimeout(timeout * 1000)
    // .build();
    // httpPost.setConfig(requestConfig);

    // if (body != null && !body.isEmpty()) {
    // StringEntity entity = new StringEntity(body);
    // httpPost.setEntity(entity);
    // }

    // if (contentType != null) {
    // httpPost.setHeader("Content-Type", contentType);
    // }
    // setCookies(httpPost, cookies);

    // // Refactor the logging function to accept the body as a string.
    // logRequest(httpPost, body); // Log the request

    // return httpClient.execute(httpPost);
    // }

    public static CloseableHttpResponse sendPostRequest(String url, List<String> cookies, Integer timeout)
            throws Exception {
        return sendPostRequest(url, null, cookies, null, timeout);
    }

    public static CloseableHttpResponse sendPostRequest(String url, String body, List<String> cookies,
            String contentType, Integer timeout) throws Exception {

        if (timeout == null) {
            timeout = 15; // 15 seconds
        }

        // Use HttpClient from the pool
        CloseableHttpClient httpClient = HttpClientPool.getHttpClient();
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

        // Log the request
        logRequest(httpPost, body);

        return httpClient.execute(httpPost);
    }

    public static CloseableHttpResponse sendGetRequest(String url, List<String> cookies, Integer timeout)
            throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        setCookies(httpGet, cookies);

        logRequest(httpGet, null); // Log the request with no body
        return httpClient.execute(httpGet);
    }

    private static void setCookies(HttpUriRequest request, List<String> cookies) {
        if (cookies != null && !cookies.isEmpty()) {
            for (String cookie : cookies) {
                request.addHeader("Cookie", cookie);
            }
        }
    }

    public static String redactASPNetSessionId(String headerValue) {
        String prefix = "ASP.NET_SessionId=";
        int startIndex = headerValue.indexOf(prefix);

        if (startIndex == -1) {
            return headerValue; // No session ID found
        }

        int sessionIdStart = startIndex + prefix.length();
        int sessionIdEnd = headerValue.indexOf(';', sessionIdStart);

        if (sessionIdEnd == -1) {
            sessionIdEnd = headerValue.length();
        }

        String sessionId = headerValue.substring(sessionIdStart, sessionIdEnd);

        if (sessionId.length() <= 6) {
            // Redact the entire session ID for lengths <= 6
            return headerValue.replace(sessionId, "*****");
        }

        String redactedId = sessionId.substring(0, 3) + "*".repeat(sessionId.length() - 6)
                + sessionId.substring(sessionId.length() - 3);
        return headerValue.replace(sessionId, redactedId);
    }

    private static void logRequest(HttpRequest request, String requestBody) {
        logger.fine("----- HTTP Request Start -----");
        logger.info(request.getRequestLine().toString());

        // Print all headers
        for (Header header : request.getAllHeaders()) {
            String headerName = header.getName();
            String headerValue = header.getValue();

            if ("Cookie".equalsIgnoreCase(headerName)) {
                headerValue = redactASPNetSessionId(headerValue);
            }

            logger.fine(headerName + ": " + headerValue);
        }

        // Log the request body if provided
        if (requestBody != null && !requestBody.isEmpty()) {
            logger.finest(redactOTPString(requestBody));
        }

        logger.fine("----- HTTP Request End -----");
    }

    public static String redactOTPString(String input) {
        String markerStart = "<OTP>";
        String markerEnd = "</OTP>";

        return redactTextWithinMarkers(input, markerStart, markerEnd);
    }

    public static String redactTextWithinMarkers(String input, String markerStart, String markerEnd) {
        // Construct the regular expression pattern to match text within markers
        String regex = Pattern.quote(markerStart) + "(.*?)" + Pattern.quote(markerEnd);

        // Create a pattern object
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher object
        Matcher matcher = pattern.matcher(input);

        // Replace only the content between the markers with stars
        String output = matcher.replaceAll(markerStart + "******" + markerEnd);

        return output;
    }

}
