package com.thalesdemo.safenet.token.list.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thalesdemo.safenet.token.list.api.util.SecurityUtil;

@Service
public class ConfigService {

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Value("${operator.encrypted-email}")
    private String encryptedEmail;

    @Value("${operator.encrypted-password}")
    private String encryptedPassword;

    @Value("${bsidca.base-url}")
    private String baseUrl;

    @Value("${bsidca.virtual-server-name}")
    private String virtualServerName;

    @Value("${bsidca.scheduling.cron-inventory}")
    private String bsidcaSchedulingCronInventory;

    @Value("${bsidca.connection-timeout}")
    private Integer defaultHttpRequestTimeout;

    @Value("${bsidca.token.storage-file}")
    private String tokenStorageFile;

    public String getTokenStorageFile() {
        return tokenStorageFile;
    }

    public String getBsidcaBaseUrl() throws Exception {
        return baseUrl;
    }

    public String getOrganization() throws Exception {
        return virtualServerName;
    }

    public Integer getDefaultHttpRequestTimeout() {
        return defaultHttpRequestTimeout;
    }

    public char[] getDecryptedValue(String encryptedValue) throws Exception {
        return SecurityUtil.decrypt(encryptedValue, secretKey);
    }

    public char[] getDecryptedEmailValue() throws Exception {
        return getDecryptedValue(encryptedEmail);
    }

    public char[] getDecryptedPasswordValue() throws Exception {
        return getDecryptedValue(encryptedPassword);
    }

    public void clearSensitiveData(char[] sensitiveData) {
        SecurityUtil.clearSensitiveData(sensitiveData);
    }

}