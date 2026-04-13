package com.marzuk.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.marzuk.chat", "com.marzuk.components"},
        exclude = {UserDetailsServiceAutoConfiguration.class}
)
public class ChatApp {

    public static void main(String[] args) {
        SpringApplication.run(ChatApp.class, args);
        log.info("###################################");
        log.info("Chat service started successfully!!!");
        log.info("###################################");
    }
}
