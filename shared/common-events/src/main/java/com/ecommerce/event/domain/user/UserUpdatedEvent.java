package com.ecommerce.event.domain.user;

import com.ecommerce.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * User Updated Event
 *
 * Fired when user information is updated
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class UserUpdatedEvent extends BaseEvent {

    /**
     * User ID
     */
    private String userId;

    /**
     * Updated fields
     */
    private java.util.Map<String, Object> updatedFields;

    /**
     * Previous values for updated fields
     */
    private java.util.Map<String, Object> previousValues;

    /**
     * Update reason
     */
    private String updateReason;

    /**
     * Updated by
     */
    private String updatedBy;

    /**
     * Update timestamp
     */
    private LocalDateTime updateTimestamp;

    public UserUpdatedEvent(String sourceService) {
        super(sourceService);
    }

    @Override
    public String getEventType() {
        return "USER_UPDATED";
    }
}