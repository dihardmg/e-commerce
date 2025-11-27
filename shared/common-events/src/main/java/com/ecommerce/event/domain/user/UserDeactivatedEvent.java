package com.ecommerce.event.domain.user;

import com.ecommerce.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * User Deactivated Event
 *
 * Fired when a user account is deactivated
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class UserDeactivatedEvent extends BaseEvent {

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
     * Deactivation reason
     */
    private String deactivationReason;

    /**
     * Deactivated by
     */
    private String deactivatedBy;

    /**
     * Deactivation timestamp
     */
    private LocalDateTime deactivationTimestamp;

    /**
     * Whether deactivation is temporary
     */
    private Boolean temporary;

    /**
     * Reactivation date if temporary
     */
    private LocalDateTime reactivationDate;

    public UserDeactivatedEvent(String sourceService) {
        super(sourceService);
    }

    @Override
    public String getEventType() {
        return "USER_DEACTIVATED";
    }
}