package com.marzuk.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.marzuk.gateway", "com.marzuk.components"},
        exclude = {UserDetailsServiceAutoConfiguration.class})
public class GatewayApp {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApp.class, args);
        log.info("###################################");
        log.info("Api-Gateway service started successfully!!!");
        log.info("###################################");
    }
}
