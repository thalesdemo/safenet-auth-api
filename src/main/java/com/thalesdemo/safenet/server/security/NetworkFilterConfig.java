package com.thalesdemo.safenet.server.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "network-filter")
public class NetworkFilterConfig {

    private boolean enableIpBasedFiltering;
    private List<String> allowedIpRanges;
    private boolean enableXForwardedForFiltering;
    private boolean strictEnforcementXForwardedForFiltering;
    private List<String> allowedXForwardedForRanges;

}
