package com.marzuk.authorizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marzuk.components.exception.UnauthorizedException;
import com.marzuk.components.pojos.response.Response;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WebCookieJwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthorizerProperties authorizerProperties;
    private final ObjectMapper objectMapper;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String ROLE_CLAIM = "role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isPublicPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> token = extractTokenFromCookie(request);
        if (token.isEmpty()) {
            writeUnauthorized(response);
            return;
        }

        try {
            Claims claims = jwtUtil.validateToken(token.get());
            String userId = claims.getSubject();
            String role = claims.get(ROLE_CLAIM, String.class);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority(role)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UnauthorizedException exception) {
            writeUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> authorizerProperties.getCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private boolean isPublicPath(String requestUri) {
        return authorizerProperties.getPublicPaths().stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Response.error("Unauthorized"));
    }
}
