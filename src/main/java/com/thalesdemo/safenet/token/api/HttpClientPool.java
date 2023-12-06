package com.thalesdemo.safenet.token.api;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientPool {

    private static CloseableHttpClient httpClient;

    public static synchronized CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

            // Number of total connections you'd like to have in the pool
            cm.setMaxTotal(1000);

            // Number of connections per route (per target host)
            cm.setDefaultMaxPerRoute(1000);

            httpClient = HttpClients.custom()
                    .setConnectionManager(cm)
                    .build();
        }
        return httpClient;
    }
}