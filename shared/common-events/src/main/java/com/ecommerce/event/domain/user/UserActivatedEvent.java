package com.ecommerce.event.domain.user;

import com.ecommerce.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * User Activated Event
 *
 * Fired when a user account is activated
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class UserActivatedEvent extends BaseEvent {

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
     * Activation reason
     */
    private String activationReason;

    /**
     * Activated by
     */
    private String activatedBy;

    /**
     * Whether this is a new user activation
     */
    private Boolean newUserActivation;

    /**
     * Whether email was verified
     */
    private Boolean emailVerified;

    /**
     * Whether phone was verified
     */
    private Boolean phoneVerified;

    public UserActivatedEvent(String sourceService) {
        super(sourceService);
    }

    @Override
    public String getEventType() {
        return "USER_ACTIVATED";
    }
}