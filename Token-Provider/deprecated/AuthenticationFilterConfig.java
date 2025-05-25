package com.payme.token.deprecated;

import com.payme.internal.security.token.ServiceTokenValidator;
import com.payme.token.management.SigningKeyManager;
import com.payme.internal.security.model.RecentPublicKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFilterConfig extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer";
    private static final String CORS_PREFLIGHT_REQUEST = "OPTIONS";
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of("/api/v1/public-key");

    private final ServiceTokenValidator serviceTokenValidator;
    private final SigningKeyManager signingKeyManager;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        if(isPublicEndpoint(requestUri)){
            filterChain.doFilter(request, response);
            return;
        }

        if(request.getMethod().equals(CORS_PREFLIGHT_REQUEST)){
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String authHeader = request.getHeader(AUTH_HEADER);
        if(!isHeaderValid(authHeader)){
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if(!hasValidClaims(token)){
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasValidClaims(String token){
        RecentPublicKeys recentPublicKeys = signingKeyManager.getPublicKeyHistory();
        List<String> publicKeys = List.of(
                recentPublicKeys.currentPublicKey(),
                recentPublicKeys.previousPublicKey()
        );

        return publicKeys.stream()
                .anyMatch(publicKey -> {
                    try {
                        return serviceTokenValidator.isTokenValid(
                                token,
                                publicKey,
                                "RSA"
                        );
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
                        log.warn("Failed token validation");
                        throw new RuntimeException("There was a problem during token validation. ", exception);
                    }
                });

    }

    private boolean isHeaderValid(String authHeader){
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    private boolean isPublicEndpoint(String urlPath){
        return PUBLIC_ENDPOINTS.contains(urlPath);
    }

}
