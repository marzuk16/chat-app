package com.chatapp.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadedEvent {

    private UUID mediaId;
    private UUID uploadedBy;
    private String url;
    private String thumbnailUrl;
    private String originalMessageId;
    private Instant timestamp;
}
