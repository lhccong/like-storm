package com.cong.like.listener.thumb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author cong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThumbEvent {
    private Long userId;
    private Long blogId;
    // INCR/DECR
    private EventType type;
    private LocalDateTime eventTime;

    public enum EventType {
        INCR,
        DECR
    }
}