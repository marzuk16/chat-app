package com.marzuk.adminportal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.marzuk.adminportal", "com.marzuk.components"},
        exclude = {UserDetailsServiceAutoConfiguration.class})
public class AdminPortalApp {

    public static void main(String[] args) {
        SpringApplication.run(AdminPortalApp.class, args);
        log.info("###################################");
        log.info("Admin portal started successfully!!!");
        log.info("###################################");
    }
}
