package com.authmat.token.filter;

import com.authmat.token.exception.InvalidPublicKeyException;
import com.authmat.token.validation.contracts.PublicKeyResolver;
import com.authmat.token.validation.TokenResolver;
import com.authmat.token.exception.FailedAuthenticationException;
import com.authmat.token.exception.InvalidClaimsException;
import com.authmat.token.validation.contracts.TokenBlacklist;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StrictJwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(StrictJwtAuthenticationFilter.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final TokenResolver tokenResolver = new TokenResolver();

    private final TokenBlacklist tokenBlacklist;
    private final PublicKeyResolver publicKeyResolver;
    private final UserDetailsService userDetailsService;
    private final Set<String> excludedPaths;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            addSecurityHeaders(response);

            if (!requiresAuthentication(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = tokenResolver.extractJwtFromRequest(request)
                    .orElseThrow(()-> new FailedAuthenticationException("Unable to extract a valid token from the request."));

            String base64EncodedPK = publicKeyResolver
                    .resolve()
                    .orElseThrow(() -> new InvalidPublicKeyException("Unable to resolve the active Public Key."));

            String algorithm = Optional.ofNullable(tokenResolver.resolveHeader(token).getAlgorithm())
                            .orElseThrow(() -> new InvalidAlgorithmParameterException("Unable to resolve token's algorithm."));

            Key publicKey = tokenResolver.loadPublicKey(base64EncodedPK, algorithm);

            if (validateAndSetAuthentication(token, publicKey, request)) {
                auditLogger.info(
                        "Successful JWT authentication for user: {} from IP: {}",
                        tokenResolver.resolveClaim(token, publicKey, Claims::getSubject),
                        getClientIpAddress(request)
                );
            }

        } catch (FailedAuthenticationException e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            auditLogger.warn(
                    "Failed JWT authentication attempt from IP: {} - {}",
                    getClientIpAddress(request),
                    e.getMessage()
            );
            return;
        } catch (Exception e) {
            logger.error("Unexpected error in JWT filter", e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return excludedPaths.contains(path);
    }

    private boolean validateAndSetAuthentication(String token, Key publicKey, HttpServletRequest request)
            throws FailedAuthenticationException {

        if (!tokenResolver.isTokenValid(token, publicKey)) {
            throw new FailedAuthenticationException("Invalid or expired token");
        }

        if (tokenBlacklist.isBlacklisted(token)) {
            throw new FailedAuthenticationException("Token has been revoked");
        }

        String username = tokenResolver
                        .resolveClaim(token, publicKey, Claims::getSubject)
                        .orElseThrow(() -> new InvalidClaimsException(""));

        //String userId = tokenResolver.resolveClaim(token, publicKey, claims -> claims.get("id", String.class)).orElseThrow();
        Set<String> authorities = tokenResolver.extractAuthorities(token, publicKey);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!userDetails.isEnabled()) {
            throw new FailedAuthenticationException("User account is disabled");
        }

        UsernamePasswordAuthenticationToken authentication =
                buildToken(userDetails, username, authorities, request);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return true;
    }

    private UsernamePasswordAuthenticationToken buildToken(
            UserDetails user, String username, Set<String> authorities, HttpServletRequest request
    ){
        Set<GrantedAuthority> grantedAuthorities = authorities
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, username, grantedAuthorities);

        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return token;
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        if ("OPTIONS".equals(request.getMethod())) {
            return false;
        }

        return !isExcludedPath(request.getRequestURI());
    }

}