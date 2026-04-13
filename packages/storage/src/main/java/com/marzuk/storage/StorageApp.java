package com.marzuk.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.marzuk.storage", "com.marzuk.components"},
        exclude = {UserDetailsServiceAutoConfiguration.class})
public class StorageApp {

    public static void main(String[] args) {
        SpringApplication.run(StorageApp.class, args);
        log.info("###################################");
        log.info("Storage service started successfully!!!");
        log.info("###################################");
    }
}
