package com.thalesdemo.safenet.token.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON output
@JsonPropertyOrder({ "type", "phone_number", "email_address", "serial_number", "operating_system", "push_otp_capable",
        "unlock_time", "last_auth_attempt", "last_auth_success", "failed_attempts", "state" }) // Defines the
                                                                                               // serialization order

public class TokenDTO {
    private String type;
    @JsonProperty("phone_number")
    private String phoneNumber; // Optional

    @JsonProperty("email_address")
    private String email; // Optional

    @JsonProperty("options")
    private List<String> options;
    @JsonProperty("serial_number")
    private String serial;

    @JsonProperty("operating_system")
    private String operatingSystem;

    @JsonProperty("push_otp_capable")
    private Boolean pushCapable;

    @JsonProperty("unlock_time")
    private OffsetDateTime unlockTime;

    @JsonProperty("last_auth_attempt")
    private OffsetDateTime lastAuthDate;

    @JsonProperty("last_auth_success")
    private OffsetDateTime lastSuccessDate;

    @JsonProperty("failed_attempts")
    private Integer failedAttempts;

    @JsonProperty("remaining_attempts")
    private Integer remainingAttempts;

    @JsonProperty("num_total_failures")
    private Integer overallFailedAttempts;

    @JsonProperty("max_attempt_policy")
    private Integer maxLockoutAttempts;

    private String state;

    // Lombok @Data generates constructor, getters, setters, equals, hashCode, and
    // toString methods.

    // Custom getter for 'type' to output in lowercase
    @JsonProperty("type")
    public String getType() {
        return type != null ? type.toLowerCase() : null;
    }
}