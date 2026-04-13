package com.marzuk.authorizer;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.authorizer")
public class AuthorizerProperties {

    private Mode mode;
    private List<String> publicPaths = new ArrayList<>();
    private String cookieName = "jwt";

    public enum Mode {
        GATEWAY,
        WEB
    }
}
