package com.marzuk.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.marzuk.user", "com.marzuk.components"},
        exclude = {UserDetailsServiceAutoConfiguration.class}
)
public class UserApp {

    public static void main(String[] args) {
        SpringApplication.run(UserApp.class, args);
        log.info("###################################");
        log.info("User service started successfully!!!");
        log.info("###################################");
    }
}
