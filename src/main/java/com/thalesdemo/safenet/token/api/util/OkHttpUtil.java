package com.thalesdemo.safenet.token.api.util;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thalesdemo.safenet.token.api.service.SOAPClientService;

public class OkHttpUtil {

    private static final Logger logger = Logger.getLogger(SOAPClientService.class.getName());

    private static OkHttpClient createOkHttpClient(Integer timeout, final List<String> cookies) {
        // Implement a basic CookieJar to handle cookies
        CookieJar cookieJar = new CookieJar() {
            private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                // Note: This example does not persist cookies between application instances
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
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public static Response sendPostRequest(String url, String body, List<String> cookies, String contentType,
            Integer timeout) throws IOException {
        if (timeout == null) {
            timeout = 15; // Default to 15 seconds if timeout is null
        }

        OkHttpClient client = createOkHttpClient(timeout, cookies);

        RequestBody requestBody = RequestBody.create(body, MediaType.get(contentType));
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);

        // Adding cookies to request if any
        if (cookies != null && !cookies.isEmpty()) {
            StringBuilder cookieHeader = new StringBuilder();
            for (String cookie : cookies) {
                if (cookieHeader.length() > 0)
                    cookieHeader.append("; ");
                cookieHeader.append(cookie);
            }
            requestBuilder.addHeader("Cookie", cookieHeader.toString());
        }

        Request request = requestBuilder.build();

        // Execute the request and retrieve the response
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.warning("Request to " + request.url() + " failed with status code: " + response.code());
                // Consider whether to throw an exception or handle this situation differently
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException occurred while making HTTP request to: " + request.url(), e);
            // Depending on your error handling policy, you might throw a custom exception here
        }

        // Log the request (method placeholder, implement logging as needed)
        logRequestOkHttp(request, body);
        logResponseOkHttp(response);

        return response;
    }

    private static void logResponseOkHttp(Response response) {
        logger.fine("----- OkHttp Response Start -----");
        logger.info("Status Code: " + response.code());

        logger.fine("Response Headers:\n" + response.headers().toString());
        // Log headers
        // response.headers().names().forEach(headerName -> {
        // logger.fine(headerName + ": " + response.header(headerName));
        // });

        // Attempt to log the response body using peekBody
        final long maxPeekBytes = 1_024L; // Define max bytes to peek, adjust as necessary
        try {
            String responseBody = response.peekBody(maxPeekBytes).string();
            logger.finest("Response Body:\n" + responseBody);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading response body for logging", e);
        }

        logger.fine("----- OkHttp Response End -----");
    }

    // Placeholder method for request logging (OKHTTP)
    private static void logRequestOkHttp(Request request, String requestBody) {
        logger.fine("----- OkHttp Request Start -----");
        logger.info(request.method() + " " + request.url());

        Headers headers = request.headers();
        // Print all headers
        logger.fine("Header:\n" + request.headers().toString());
        // Log the request body if provided and not a GET request (since GET doesn't
        // have a body)
        if (requestBody != null && !requestBody.isEmpty()) {
            logger.finest(requestBody);
        } else if (request.body() != null && !"GET".equalsIgnoreCase(request.method())) {
            // Extracting the body for logging if it wasn't passed as a parameter
            Buffer buffer = new Buffer();
            try {
                request.body().writeTo(buffer);
                logger.finest(buffer.readUtf8());
            } catch (IOException e) {
                logger.warning("Failed to read request body for logging.");
            }
        }

        logger.fine("----- OkHttp Request End -----");
    }
}
