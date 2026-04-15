package com.marzuk.authorizer;

import com.marzuk.components.pojos.enums.auth.AuthorizerMode;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.authorizer")
public class AuthorizerProperties {

    private AuthorizerMode mode;
    private List<String> publicPaths = new ArrayList<>();
    private String cookieName = "jwt";
}
