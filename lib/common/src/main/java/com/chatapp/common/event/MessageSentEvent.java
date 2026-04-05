package com.chatapp.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSentEvent {

    private String messageId;
    private String conversationId;
    private UUID senderId;
    private List<UUID> recipientIds;
    private String content;
    private String messageType;
    private Instant timestamp;
}
