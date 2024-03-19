package com.thalesdemo.safenet.token.api.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TokenDetailsDTO {
    private String state;
    private OffsetDateTime unlockTime;
    private OffsetDateTime lastAuthDate;
    private OffsetDateTime lastSuccessDate;
    private int oTPTTL;
    private int otpLength;
    private int activationCount;
    private int authAttempts;
    private OffsetDateTime lastChallengeDate;
    private boolean isTimeBased;
    private int timeInterval;
    private String deviceName;
}
