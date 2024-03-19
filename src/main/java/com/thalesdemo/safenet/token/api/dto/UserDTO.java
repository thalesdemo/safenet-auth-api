package com.thalesdemo.safenet.token.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserDTO {
    private List<Group> groups;
    private int preferredLanguage;
    private String passwordSetDate;
    private String passwordExpiryDate;
    private int passwordAttemptCount;
    private String userName;
    private String firstName;
    private String lastname; // Note: Java convention would name this "lastName"
    private String address1;
    private String city;
    private String state;
    private String country;
    private String zip;
    private String email;
    private String telephone;
    private String countrycode; // Note: Java convention would name this "countryCode"
    private String extension;
    private String mobile;
    private String fax;
    private boolean locked;
    private List<String> customAttributes;
    private String unlockAt;
    private String message;
    private boolean tempPasswordEnabled;
    private boolean tempPasswordChangeReq;
    private String containerName;
    private boolean useExternalCredentials;
    private boolean isAccountDormant;

    // Getter and Setter methods

    @Data
    public static class Group {
        private String groupName;
        private String description;
        private boolean readOnly;
        private boolean empty;

        // Getter and Setter methods
    }
}
