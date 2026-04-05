package com.chatapp.common.config;

public final class KafkaTopicConfig {

    private KafkaTopicConfig() {}

    public static final String USER_REGISTERED = "user.registered";
    public static final String USER_UPDATED = "user.updated";
    public static final String MESSAGE_SENT = "message.sent";
    public static final String TYPING_INDICATOR = "typing.indicator";
    public static final String PRESENCE_CHANGED = "presence.changed";
    public static final String MEDIA_UPLOADED = "media.uploaded";
}
