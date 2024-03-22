package com.thalesdemo.safenet.token.api.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class OkHttpUtil {

    private OkHttpUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger logger = Logger.getLogger(OkHttpUtil.class.getName());

    private static OkHttpClient createOkHttpClient(Integer timeout) {
        // Implement a basic CookieJar to handle cookies
        CookieJar cookieJar = new CookieJar() {
            private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<>();
            }
        };

        return new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(timeout != null ? timeout : 15, TimeUnit.SECONDS)
                .readTimeout(timeout != null ? timeout : 15, TimeUnit.SECONDS)
                .writeTimeout(timeout != null ? timeout : 15, TimeUnit.SECONDS)
                .build();
    }

    public static Response sendGetRequest(String url, List<String> cookies, Integer timeout) throws IOException {
        OkHttpClient client = createOkHttpClient(timeout);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        // Execute the request and retrieve the response
        return executeRequest(client, requestBuilder, cookies);
    }

    public static Response sendPostRequest(String url, String body, List<String> cookies, String contentType,
            Integer timeout) throws IOException {
        OkHttpClient client = createOkHttpClient(timeout);

        RequestBody requestBody = RequestBody.create(body != null ? body : "", MediaType.get(contentType));
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);

        if (contentType != null) {
            requestBuilder.addHeader("Content-Type", contentType);
        }

        // Execute the request and retrieve the response
        return executeRequest(client, requestBuilder, cookies);
    }

    private static Response executeRequest(OkHttpClient client, Request.Builder requestBuilder, List<String> cookies)
            throws IOException {
        // Manually add cookies if they are provided
        if (cookies != null && !cookies.isEmpty()) {
            StringBuilder cookieHeader = new StringBuilder();
            for (String cookie : cookies) {
                if (cookieHeader.length() > 0) {
                    cookieHeader.append("; ");
                }
                cookieHeader.append(cookie);
            }
            requestBuilder.addHeader("Cookie", cookieHeader.toString());
        }

        Request request = requestBuilder.build();
        logRequestOkHttp(request, request.method().equals("GET") ? null : request.body().toString());

        Response response = null;

        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.warning(request.method() + " request to " + request.url() + " failed with status code: "
                        + response.code());
            }
        } catch (IOException e) {
            String errorString = "IOException occurred while making HTTP request to: " + request.url();
            logger.log(Level.SEVERE, errorString, e);
            throw new IOException(errorString); // Re-throw the exception to allow caller to handle it
        }

        logResponseOkHttp(response);

        return response;
    }

    private static void logResponseOkHttp(Response response) {
        logger.fine("----- OkHttp Response Start -----");
        logger.info("HTTP response code: " + response.code());
        logger.log(Level.FINE, "Response Headers:\n{0}", response.headers());

        // Attempt to log the response body using peekBody
        final long maxPeekBytes = 1_024L; // Define max bytes to peek, adjust as necessary
        try {
            String responseBody = response.peekBody(maxPeekBytes).string();
            logger.log(Level.FINEST, "Response Body:\n{0}", responseBody);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading response body for logging", e);
        }

        logger.fine("----- OkHttp Response End -----");
    }

    // Placeholder method for request logging (OKHTTP)
    private static void logRequestOkHttp(Request request, String requestBody) {
        logger.fine("----- OkHttp Request Start -----");
        logger.info(request.method() + " " + request.url());

        // Print all headers
        logger.log(Level.FINE, "Request Headers:\n{0}", request.headers());
        // Log the request body if provided and not a GET request (since GET doesn't
        // have a body)
        if (requestBody != null && !requestBody.isEmpty()) {
            logger.finest(requestBody);
        } else if (request.body() != null && !"GET".equalsIgnoreCase(request.method())) {
            // Extracting the body for logging if it wasn't passed as a parameter
            Buffer buffer = new Buffer();
            try {
                request.body().writeTo(buffer);
                if (buffer.readUtf8().length() > 0) {
                    String requestBodyString = buffer.readUtf8();
                    logger.finest(requestBodyString);
                }
            } catch (IOException e) {
                logger.warning("Failed to read request body for logging.");
            }
        }

        logger.fine("----- OkHttp Request End -----");
    }
}
