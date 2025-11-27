package com.ecommerce.event.domain.user;

import com.ecommerce.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * User Registered Event
 *
 * Fired when a new user is registered
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class UserRegisteredEvent extends BaseEvent {

    /**
     * User ID
     */
    private String userId;

    /**
     * Username
     */
    private String username;

    /**
     * Email
     */
    private String email;

    /**
     * First name
     */
    private String firstName;

    /**
     * Last name
     */
    private String lastName;

    /**
     * Phone number
     */
    private String phoneNumber;

    /**
     * User roles
     */
    private java.util.List<String> roles;

    /**
     * Registration source
     */
    private String registrationSource;

    /**
     * Whether email verification is required
     */
    private Boolean emailVerificationRequired;

    /**
     * Whether phone verification is required
     */
    private Boolean phoneVerificationRequired;

    public UserRegisteredEvent(String sourceService) {
        super(sourceService);
    }

    @Override
    public String getEventType() {
        return "USER_REGISTERED";
    }
}