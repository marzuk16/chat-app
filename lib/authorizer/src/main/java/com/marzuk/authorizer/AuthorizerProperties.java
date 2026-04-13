package com.marzuk.authorizer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.authorizer")
public class AuthorizerProperties {

    private Mode mode;
    private List<String> publicPaths = new ArrayList<>();
    private String cookieName = "jwt";

    public enum Mode {
        GATEWAY, WEB
    }
}
