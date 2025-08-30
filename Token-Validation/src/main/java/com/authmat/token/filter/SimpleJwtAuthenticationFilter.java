package com.authmat.token.filter;

import com.authmat.token.exception.FailedAuthenticationException;
import com.authmat.token.exception.InvalidClaimsException;
import com.authmat.token.exception.InvalidPublicKeyException;
import com.authmat.token.model.AuthUser;
import com.authmat.token.validation.TokenResolver;
import com.authmat.token.validation.contracts.PublicKeyResolver;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class SimpleJwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJwtAuthenticationFilter.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");
    private static final TokenResolver TOKEN_RESOLVER = new TokenResolver();

    private final PublicKeyResolver publicKeyResolver;
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

            String token = TOKEN_RESOLVER.extractJwtFromRequest(request)
                    .orElseThrow(()-> new FailedAuthenticationException("Unable to extract a valid token from the request."));

            String kid = TOKEN_RESOLVER.resolveHeader(token).getKeyId();

            String base64EncodedPK = publicKeyResolver
                    .resolve(kid)
                    .orElseThrow(() -> new InvalidPublicKeyException("Unable to resolve the active Public Key."));

            String algorithm = Optional.ofNullable(TOKEN_RESOLVER.resolveHeader(token).getAlgorithm())
                    .orElseThrow(() -> new InvalidAlgorithmParameterException("Unable to resolve token's algorithm."));

            Key publicKey = TOKEN_RESOLVER.loadPublicKey(base64EncodedPK, algorithm);

            if (validateAndSetAuthentication(token, publicKey, request)) {
                AUDIT_LOGGER.info(
                        "Successful JWT authentication for user: {} from IP: {}",
                        TOKEN_RESOLVER.resolveClaim(token, publicKey, Claims::getSubject),
                        getClientIpAddress(request)
                );
            }

        } catch (FailedAuthenticationException e) {
            LOGGER.warn("JWT token validation failed: {}", e.getMessage());
            AUDIT_LOGGER.warn(
                    "Failed JWT authentication attempt from IP: {} - {}",
                    getClientIpAddress(request),
                    e.getMessage()
            );
            return;
        } catch (Exception e) {
            LOGGER.error("Unexpected error in JWT filter", e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return excludedPaths.contains(path);
    }

    private boolean validateAndSetAuthentication(String token, Key publicKey, HttpServletRequest request)
            throws FailedAuthenticationException {

        if (!TOKEN_RESOLVER.isTokenValid(token, publicKey)) {
            throw new FailedAuthenticationException("Invalid or expired token");
        }

        String username = TOKEN_RESOLVER
                .resolveClaim(token, publicKey, Claims::getSubject)
                .orElseThrow(() -> new InvalidClaimsException(""));

        //String userId = tokenResolver.resolveClaim(token, publicKey, claims -> claims.get("id", String.class)).orElseThrow();
        Set<String> authorities = TOKEN_RESOLVER.extractAuthorities(token, publicKey);

        UserDetails userDetails = AuthUser.builder()
                        .username(username)
                        .authorities(authorities)
                        .isEnabled(true)
                        .build();

        UsernamePasswordAuthenticationToken authentication =
                buildToken(userDetails, username, request);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return true;
    }

    private UsernamePasswordAuthenticationToken buildToken(UserDetails user, String username, HttpServletRequest request){
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, username, user.getAuthorities());

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
        if ("OPTIONS".equals(request.getMethod())) return false;

        return !isExcludedPath(request.getRequestURI());
    }
}
