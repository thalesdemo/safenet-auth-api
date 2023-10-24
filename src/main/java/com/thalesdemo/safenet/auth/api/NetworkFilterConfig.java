package com.thalesdemo.safenet.auth.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void postConstruct() {
        System.out.println("==== NetworkFilterConfig Initialization ====");
        System.out.println("Enable IP Based Filtering: " + enableIpBasedFiltering);
        System.out.println("Allowed IP Ranges: " + allowedIpRanges);
        System.out.println("Enable X-Forwarded-For Filtering: " + enableXForwardedForFiltering);
        System.out.println("Strict Enforcement X-Forwarded-For Filtering: " + strictEnforcementXForwardedForFiltering);
        System.out.println("Allowed X-Forwarded-For Ranges: " + allowedXForwardedForRanges);
    }
}
