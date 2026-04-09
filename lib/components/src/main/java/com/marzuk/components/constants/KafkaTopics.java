package com.marzuk.components.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class KafkaTopics {

    public static final String USER_REGISTERED = "user.registered";
    public static final String USER_LOCKED = "user.locked";
    public static final String USER_PRESENCE_CHANGED = "user.presence-changed";
    public static final String MESSAGE_SENT = "message.sent";
    public static final String CHAT_USER_ACTIVITY = "chat.user-activity";
    public static final String ADMIN_BROADCAST = "admin.broadcast";
}
