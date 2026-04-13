package com.marzuk.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.marzuk.auth", "com.marzuk.components"},
        exclude = {UserDetailsServiceAutoConfiguration.class})
public class AuthApp {

    public static void main(String[] args) {
        SpringApplication.run(AuthApp.class, args);
        log.info("###################################");
        log.info("Auth service started successfully!!!");
        log.info("###################################");
    }
}
