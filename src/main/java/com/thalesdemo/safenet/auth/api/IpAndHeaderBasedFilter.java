package com.thalesdemo.safenet.auth.api;

import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
 * The filter also ensures it doesn't process the same request multiple times
 * by setting and checking a request attribute.
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

    // Logger instance
    private static final Logger logger = Logger.getLogger(IpAndHeaderBasedFilter.class.getName());

    // Attribute to ensure request is processed only once
    private static final String FILTER_PROCESSED_REQUEST_ATTR = "IpAndHeaderBasedFilter.processed";

    @PostConstruct
    public void initSubnets() {
        this.allowedIpMatchers = config.getAllowedIpRanges().stream()
                .map(IpAddressMatcher::new)
                .collect(Collectors.toList());

        this.allowedXForwardedMatchers = config.getAllowedXForwardedForRanges().stream()
                .map(IpAddressMatcher::new)
                .collect(Collectors.toList());

        logger.info("Network Config Initialized: " + config.toString());
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if this filter already processed the request
        if (httpRequest.getAttribute(FILTER_PROCESSED_REQUEST_ATTR) != null) {
            chain.doFilter(request, response);
            return;
        }

        logger.info("Headers: " + Collections.list(httpRequest.getHeaderNames()).stream()
                .map(headerName -> headerName + ": " + httpRequest.getHeader(headerName))
                .collect(Collectors.joining(", ")));

        if (config.isEnableIpBasedFiltering()) {
            String remoteIp = httpRequest.getRemoteAddr();
            logger.info("Checking IP: " + remoteIp + " for path: " + httpRequest.getRequestURI());
            if (!isIpAllowed(remoteIp, allowedIpMatchers)) {
                httpResponse.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden IP");
                return;
            }
        }

        if (config.isEnableXForwardedForFiltering()) {
            String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
            if (xForwardedFor != null) {
                logger.info("Checking X-Forwarded-For IP: " + xForwardedFor.split(",")[0].trim());
                if (!isIpAllowed(xForwardedFor.split(",")[0].trim(), allowedXForwardedMatchers)) {
                    httpResponse.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden X-Forwarded-For IP");
                    return;
                }
            } else if (config.isStrictEnforcementXForwardedForFiltering()) {
                httpResponse.sendError(HttpStatus.FORBIDDEN.value(), "Missing X-Forwarded-For Header");
                return;
            }
        }

        // Mark this request as processed by this filter
        httpRequest.setAttribute(FILTER_PROCESSED_REQUEST_ATTR, true);

        chain.doFilter(request, response);
    }

    /**
     * Checks if a given IP is allowed based on a list of allowed IP matchers.
     *
     * @param ip              The IP address to check.
     * @param allowedMatchers A list of allowed IP matchers.
     * @return true if the IP is allowed, false otherwise.
     */
    private boolean isIpAllowed(String ip, List<IpAddressMatcher> allowedMatchers) {
        return allowedMatchers.stream().anyMatch(matcher -> matcher.matches(ip));
    }

    @Override
    public void destroy() {
    }
}
