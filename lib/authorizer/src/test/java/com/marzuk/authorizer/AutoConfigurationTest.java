package com.marzuk.authorizer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigurationTest {

    private static final String PUBLIC_KEY;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            var keyPair = generator.generateKeyPair();
            PUBLIC_KEY = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to generate test RSA keys", exception);
        }
    }

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    WebMvcAutoConfiguration.class,
                    SecurityAutoConfiguration.class,
                    AuthorizerAutoConfiguration.class));

    @Test
    void gatewayMode_createsGatewayFilterAndJwtUtil() {
        contextRunner
                .withPropertyValues(
                        "app.authorizer.mode=gateway",
                        "app.authorizer.public-paths=/api/auth/**,/actuator/health",
                        "jwt.public-key=" + PUBLIC_KEY
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtUtil.class);
                    assertThat(context).hasSingleBean(GatewayJwtFilter.class);
                    assertThat(context).doesNotHaveBean(WebCookieJwtFilter.class);
                });
    }

    @Test
    void webMode_createsWebFilterAndJwtUtil() {
        contextRunner
                .withPropertyValues(
                        "app.authorizer.mode=web",
                        "app.authorizer.public-paths=/login,/actuator/health",
                        "app.authorizer.cookie-name=jwt",
                        "jwt.public-key=" + PUBLIC_KEY
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtUtil.class);
                    assertThat(context).hasSingleBean(WebCookieJwtFilter.class);
                    assertThat(context).doesNotHaveBean(GatewayJwtFilter.class);
                });
    }

    @Test
    void noMode_createsNeitherFilter() {
        contextRunner
                .withPropertyValues("jwt.public-key=" + PUBLIC_KEY)
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtUtil.class);
                    assertThat(context).doesNotHaveBean(GatewayJwtFilter.class);
                    assertThat(context).doesNotHaveBean(WebCookieJwtFilter.class);
                });
    }

    @Test
    void noPublicKey_doesNotCreateJwtUtil() {
        contextRunner
                .run(context -> assertThat(context).doesNotHaveBean(JwtUtil.class));
    }
}
