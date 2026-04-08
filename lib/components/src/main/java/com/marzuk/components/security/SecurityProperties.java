package com.marzuk.components.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private List<String> publicPaths = new ArrayList<>();
}
