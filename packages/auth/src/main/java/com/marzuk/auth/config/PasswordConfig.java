package com.marzuk.auth.config;

import com.marzuk.auth.security.PepperingPasswordEncoder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    @Primary
    public PasswordEncoder pepperingPasswordEncoder(
            @Qualifier("passwordEncoder") PasswordEncoder bcryptPasswordEncoder,
            @Value("${app.security.pepper}") String pepper) {
        return new PepperingPasswordEncoder(bcryptPasswordEncoder, pepper);
    }
}
