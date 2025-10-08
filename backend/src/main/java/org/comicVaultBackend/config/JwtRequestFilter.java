package org.comicVaultBackend.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    public List<String> freeCookieEndpoints = new ArrayList<>(
            Arrays.asList(
                    "/users/authenticate",
                    "/users/registerFirstAdmin",
                    "/users/adminUserExists",
                    "/users/refresh",
                    "/users/forgetMe",
                    "/health",
                    "/v3/api-docs",
                    "/api-docs",
                    "/swagger-ui"
            )
    );

    private boolean isFreeCookieEndpoint(String requestUri) {
        return freeCookieEndpoints.stream()
                .anyMatch(requestUri::contains);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Skip JWT/Cookie validation for free endpoints
        if (isFreeCookieEndpoint(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        String jwt = extractAccessTokenFromCookies(request);
        String refreshToken = extractRefreshTokenFromCookies(request);
        String username = null;

        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                // Access token expired → let refresh endpoint handle renewal
            } catch (Exception e) {
                // Invalid token → ignore
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            String role = String.valueOf(jwtUtil.extractRole(jwt));

            List<GrantedAuthority> authorities = Stream.of(role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            if (jwtUtil.validateAccessToken(jwt, userDetails) && jwtUtil.validateRefreshToken(refreshToken)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        chain.doFilter(request, response);
    }

    private String extractAccessTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("ACCESS_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("REFRESH_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
