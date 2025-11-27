package com.ecommerce.event.domain.user;

import com.ecommerce.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * User Deleted Event
 *
 * Fired when a user is deleted
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class UserDeletedEvent extends BaseEvent {

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
     * Reason for deletion
     */
    private String deletionReason;

    /**
     * Soft delete indicator
     */
    private Boolean softDelete;

    /**
     * Deleted by
     */
    private String deletedBy;

    /**
     * Deletion timestamp
     */
    private LocalDateTime deletionTimestamp;

    public UserDeletedEvent(String sourceService) {
        super(sourceService);
    }

    @Override
    public String getEventType() {
        return "USER_DELETED";
    }
}