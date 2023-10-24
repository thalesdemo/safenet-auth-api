package com.thalesdemo.safenet.auth.api;

import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletResponseWrapper;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This filter enforces IP and X-Forwarded-For header based filtering.
 * It uses the configuration provided to check if incoming requests should be
 * allowed or not.
 *
 * IP filtering checks the actual IP of the incoming request while
 * X-Forwarded-For filtering checks the IP provided in the X-Forwarded-For
 * header.
 *
 * Requests are logged with the following format:
 * <HTTP METHOD> | Remote-IP: REMOTE-IP | X-Forwarded-For: X-FORWARDED-FOR-IP |
 * URL: URL | rc=RETURN CODE
 *
 * @author YourName
 */
@Component
@DependsOn("networkFilterConfig")
public class IpAndHeaderBasedFilter implements Filter {

    @Autowired
    private NetworkFilterConfig config;

    private List<IpAddressMatcher> allowedIpMatchers;
    private List<IpAddressMatcher> allowedXForwardedMatchers;
    private static final String FILTER_PROCESSED_REQUEST_ATTR = "FILTER_PROCESSED_REQUEST";

    // Logger instance
    private static final Logger logger = Logger.getLogger(IpAndHeaderBasedFilter.class.getName());

    /**
     * Initialize the allowed IP matchers for both direct IP
     * and X-Forwarded-For header checks after bean properties are set.
     */
    @PostConstruct
    public void initSubnets() {
        this.allowedIpMatchers = config.getAllowedIpRanges().stream()
                .map(IpAddressMatcher::new)
                .collect(Collectors.toList());

        this.allowedXForwardedMatchers = config.getAllowedXForwardedForRanges().stream()
                .map(IpAddressMatcher::new)
                .collect(Collectors.toList());

        logger.log(Level.INFO, "Network Config {0}", config.toString());
    }

    /**
     * Called by the web container to indicate to a filter that it
     * is being placed into service. No specific logic here for now.
     *
     * @param filterConfig the filter configuration object.
     * @throws ServletException in case of an initialization failure.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * The main method of the filter where the logic to check the IP address
     * and the X-Forwarded-For header is applied. If the request doesn't match
     * the allowed addresses or headers, it returns a forbidden error.
     * All requests are also logged with the desired format.
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @param chain    the filter chain.
     * @throws IOException      in case of an I/O error.
     * @throws ServletException in case of a general service failure.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if the request has already been processed by this filter
        if (httpRequest.getAttribute(FILTER_PROCESSED_REQUEST_ATTR) != null) {
            chain.doFilter(request, response);
            return;
        }

        // Use a custom response wrapper to capture the status code for logging
        StatusResponseWrapper responseWrapper = new StatusResponseWrapper(httpResponse);

        // Check if IP-based filtering is enabled and process accordingly
        if (config.isEnableIpBasedFiltering()) {
            String remoteIp = httpRequest.getRemoteAddr();
            if (!isIpAllowed(remoteIp, allowedIpMatchers)) {
                setErrorResponse(httpResponse, HttpStatus.FORBIDDEN.value(), "Forbidden IP");
                logRequestInfo(httpRequest, responseWrapper);
                return;
            }
        }

        // Check if X-Forwarded-For header filtering is enabled and process accordingly
        if (config.isEnableXForwardedForFiltering()) {
            String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
            if (xForwardedFor != null) {
                if (!isIpAllowed(xForwardedFor.split(",")[0].trim(), allowedXForwardedMatchers)) {
                    setErrorResponse(httpResponse, HttpStatus.FORBIDDEN.value(), "Forbidden X-Forwarded-For IP");
                    logRequestInfo(httpRequest, responseWrapper);
                    return;
                }
            } else if (config.isStrictEnforcementXForwardedForFiltering()) {
                setErrorResponse(httpResponse, HttpStatus.FORBIDDEN.value(), "Missing X-Forwarded-For Header");
                logRequestInfo(httpRequest, responseWrapper);
                return;
            }
        }

        // Mark the request as processed by this filter before proceeding
        httpRequest.setAttribute(FILTER_PROCESSED_REQUEST_ATTR, true);

        // Continue with the filter chain
        chain.doFilter(request, responseWrapper);

        // Log the request information after processing
        logRequestInfo(httpRequest, responseWrapper);
    }

    /**
     * Sets an error response for the given HTTP response object.
     * The response will be in a JSON format: { "error": "provided error message" }
     *
     * @param httpResponse The HTTP response object to set the error on.
     * @param statusCode   The HTTP status code to set for the error.
     * @param message      The error message to be included in the JSON response.
     * @throws IOException In case of an I/O error while writing to the response.
     */
    private void setErrorResponse(HttpServletResponse httpResponse, int statusCode, String message) throws IOException {
        httpResponse.setStatus(statusCode);
        httpResponse.setContentType("application/json");
        String jsonErrorMessage = String.format("{\"error\": \"%s\"}", message);
        httpResponse.getWriter().write(jsonErrorMessage);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    /**
     * A custom wrapper around HttpServletResponse to capture the status code.
     */
    private static class StatusResponseWrapper extends HttpServletResponseWrapper {
        private int httpStatus;

        public StatusResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int sc) throws IOException {
            httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            httpStatus = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void setStatus(int sc) {
            httpStatus = sc;
            super.setStatus(sc);
        }

        public int getStatus() {
            return httpStatus;
        }
    }

    /**
     * Checks if a given IP is allowed based on a list of allowed matchers.
     *
     * @param ip              The IP address to check.
     * @param allowedMatchers A list of allowed matchers.
     * @return true if the IP is allowed, false otherwise.
     */
    private boolean isIpAllowed(String ip, List<IpAddressMatcher> allowedMatchers) {
        return allowedMatchers.stream().anyMatch(matcher -> matcher.matches(ip));
    }

    /**
     * Logs the request information in the desired format.
     *
     * @param httpRequest     the HTTP request.
     * @param responseWrapper the wrapped HTTP response to get status.
     */
    private void logRequestInfo(HttpServletRequest httpRequest, StatusResponseWrapper responseWrapper) {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor == null)
            xForwardedFor = "N/A";

        String logMessage = String.format("%s | Remote-IP: %s | X-Forwarded-For: %s | URL: %s | rc=%d",
                httpRequest.getMethod(), httpRequest.getRemoteAddr(), xForwardedFor,
                httpRequest.getRequestURI(), responseWrapper.getStatus());

        logger.info(logMessage);
    }

    /**
     * Called by the web container to indicate to a filter that it
     * is being taken out of service. No specific logic here for now.
     */
    @Override
    public void destroy() {
    }
}
