package com.marzuk.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.marzuk.notification", "com.marzuk.components"},
        exclude = {UserDetailsServiceAutoConfiguration.class})
public class NotificationApp {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApp.class, args);
        log.info("###################################");
        log.info("Notification service started successfully!!!");
        log.info("###################################");
    }
}
