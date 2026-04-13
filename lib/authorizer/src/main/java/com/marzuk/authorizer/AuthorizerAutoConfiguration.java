package com.marzuk.authorizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration
@EnableConfigurationProperties({AuthorizerProperties.class, JwtProperties.class})
public class AuthorizerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "jwt.public-key")
    public JwtUtil jwtUtil(JwtProperties jwtProperties) {
        return new JwtUtil(jwtProperties);
    }

    @Configuration
    @ConditionalOnProperty(name = "app.authorizer.mode", havingValue = "gateway")
    static class GatewaySecurityConfig {

        @Bean
        public GatewayJwtFilter gatewayJwtFilter(JwtUtil jwtUtil, AuthorizerProperties authorizerProperties,
                                                 ObjectMapper objectMapper) {
            return new GatewayJwtFilter(jwtUtil, authorizerProperties, objectMapper);
        }

        @Bean
        public SecurityFilterChain gatewaySecurityFilterChain(HttpSecurity http,
                                                              AuthorizerProperties authorizerProperties,
                                                              GatewayJwtFilter gatewayJwtFilter) throws Exception {
            String[] publicPaths = authorizerProperties.getPublicPaths().toArray(new String[0]);

            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(requests -> requests
                            .requestMatchers(publicPaths).permitAll()
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(gatewayJwtFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "app.authorizer.mode", havingValue = "web")
    static class WebSecurityConfig {

        @Bean
        public WebCookieJwtFilter webCookieJwtFilter(JwtUtil jwtUtil, AuthorizerProperties authorizerProperties,
                                                     ObjectMapper objectMapper) {
            return new WebCookieJwtFilter(jwtUtil, authorizerProperties, objectMapper);
        }

        @Bean
        public SecurityFilterChain webSecurityFilterChain(HttpSecurity http,
                                                          AuthorizerProperties authorizerProperties,
                                                          WebCookieJwtFilter webCookieJwtFilter) throws Exception {
            String[] publicPaths = authorizerProperties.getPublicPaths().toArray(new String[0]);

            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(requests -> requests
                            .requestMatchers(publicPaths).permitAll()
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(webCookieJwtFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        }
    }
}
