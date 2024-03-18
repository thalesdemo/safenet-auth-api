package com.thalesdemo.safenet.token.api.util;

import java.util.List;

import com.thalesdemo.safenet.token.api.TokenDTO;

public class TokenUtils {

    private TokenUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static int calculateOverallFailedAttempts(List<TokenDTO> tokens) {
        int overallFailedAttempts = 0;
        for (TokenDTO token : tokens) {
            if (!"suspended".equals(token.getState()) && token.getFailedAttempts() > overallFailedAttempts) {
                overallFailedAttempts = token.getFailedAttempts();
            }
        }
        return overallFailedAttempts;
    }

    public static boolean tokenEligibleForUnlock(List<TokenDTO> tokens) {
        for (TokenDTO token : tokens) {
            if ("unlock_eligible".equals(token.getState())) {
                return true;
            }
        }
        return false;
    }

}