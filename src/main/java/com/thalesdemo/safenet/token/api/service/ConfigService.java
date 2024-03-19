package com.thalesdemo.safenet.token.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thalesdemo.safenet.token.api.util.SecurityUtil;

@Service
public class ConfigService {

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Value("${safenet.operator.encrypted-email}")
    private String encryptedEmail;

    @Value("${safenet.operator.encrypted-password}")
    private String encryptedPassword;

    @Value("${safenet.bsidca.base-url}")
    private String baseUrl;

    @Value("${safenet.bsidca.virtual-server-name}")
    private String virtualServerName;

    @Value("${safenet.bsidca.scheduling.cron-inventory}")
    private String bsidcaSchedulingCronInventory;

    @Value("${safenet.bsidca.connection-timeout}")
    private Integer defaultHttpRequestTimeout;

    @Value("${safenet.bsidca.token.storage-file}")
    private String tokenStorageFile;

    @Value("${safenet.user-lockout.max-failed-attempts}")
    private Integer userLockoutPolicy;

    public Integer getUserLockoutPolicy() {
        return userLockoutPolicy;
    }

    public String getTokenStorageFile() {
        return tokenStorageFile;
    }

    public String getBsidcaBaseUrl() {
        return baseUrl;
    }

    public String getOrganization() {
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

    public String getEncryptionSecretKey() {
        return secretKey;
    }

}