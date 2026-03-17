package com.authmat.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * <h2>GatewayIdentityFilter</h2>
 *
 * <p>
 * A servlet filter responsible for establishing the authenticated user identity
 * inside downstream services based on headers injected by the API gateway.
 * </p>
 *
 * <p>
 * This filter assumes that JWT validation has already been performed by the gateway.
 * After validation, the gateway forwards trusted identity information via HTTP headers.
 * The filter then converts this information into a Spring Security {@code SecurityContext}.
 * </p>
 *
 * <p>
 * This component lives in the <b>internal-libs</b> repository and is shared across
 * all downstream services (e.g., AuthMat, DocKeep).
 * </p>
 *
 * <h3>Responsibilities</h3>
 *
 * <ol>
 *   <li>
 *     <b>Gateway verification</b>
 *     <p>
 *     Ensures the request actually originated from the trusted API gateway by verifying
 *     a shared secret using a constant-time comparison.
 *     </p>
 *
 *     <p>
 *     This mechanism serves as a temporary trust bridge until a service mesh with
 *     mutual TLS (mTLS) is introduced.
 *     </p>
 *
 *     <p>
 *     <b>Kubernetes migration path:</b>
 *     When a service mesh such as Istio is adopted, this verification step can be
 *     removed entirely. The {@code verifyGatewaySecret()} logic becomes unnecessary
 *     because identity trust will be enforced at the network layer through:
 *     </p>
 *
 *     <ul>
 *       <li>Istio mTLS between services</li>
 *       <li>Istio {@code AuthorizationPolicy}</li>
 *     </ul>
 *
 *     <p>
 *     Removing this verification requires no additional code changes in downstream
 *     services.
 *     </p>
 *   </li>
 *
 *   <li>
 *     <b>SecurityContext population</b>
 *     <p>
 *     Reads gateway-injected identity headers and constructs a Spring Security
 *     authentication object which is placed into the {@code SecurityContext}.
 *     </p>
 *   </li>
 * </ol>
 *
 * <h3>Gateway Header Contract</h3>
 *
 * <p>
 * The gateway injects the following headers after successful JWT validation:
 * </p>
 *
 * <ul>
 *   <li>
 *     <b>X-Gateway-Secret</b> — Shared HMAC secret used for constant-time verification
 *   </li>
 *   <li>
 *     <b>X-Authenticated-User</b> — Validated JWT subject claim (user identifier)
 *   </li>
 *   <li>
 *     <b>X-Authenticated-Roles</b> — Comma-separated authorities
 *     (e.g. {@code ROLE_ADMIN,ROLE_USER})
 *   </li>
 *   <li>
 *     <b>X-Request-Id</b> — Trace or correlation ID for distributed observability
 *   </li>
 * </ul>
 *
 * <h3>Path Handling</h3>
 *
 * <p>
 * This filter executes only for endpoints that require authentication.
 * Public endpoints (e.g., {@code /auth/login}, {@code /actuator/health}) are excluded
 * within each service's {@code SecurityFilterChain}. Path filtering is intentionally
 * not handled by this filter in order to keep responsibilities clearly separated.
 * </p>
 */
@Slf4j
public class GatewayIdentityFilter extends OncePerRequestFilter {
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public static final String GATEWAY_SECRET_HEADER = "X-Gateway-Secret";
    public static final String USER_HEADER           = "X-Authenticated-User";
    public static final String REQUEST_ID_HEADER     = "X-Request-Id";
    public static final String FORWARDED_IP_HEADER   = "X-Forwarded-For";

    private final byte[] expectedSecretBytes;
    private final ObjectMapper objectMapper;
    private final AuthorityResolver authorityResolver;

    public GatewayIdentityFilter(
            String expectedGatewaySecret,
            ObjectMapper objectMapper,
            AuthorityResolver authorityResolver
    ) {
        Assert.hasText(expectedGatewaySecret, "expectedGatewaySecret must not be blank");
        this.expectedSecretBytes = expectedGatewaySecret.getBytes(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;
        this.authorityResolver = authorityResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        String clientIp = resolveClientIp(request);

        MDC.put("requestId", requestId);
        MDC.put("clientIp",  clientIp);
        MDC.put("method",    request.getMethod());
        MDC.put("uri",       request.getRequestURI());

        try {
            addSecurityHeaders(response);
            verifyGatewaySecret(request);

            String userId = Optional.ofNullable(request.getHeader(USER_HEADER))
                    .filter(str -> !str.isBlank())
                    .map(String::strip)
                    .orElseThrow(() -> new MissingIdentityHeaderException(USER_HEADER));

            MDC.put("userId", userId);

            Set<GrantedAuthority> authorities = authorityResolver.resolve(userId);

            populateSecurityContext(userId, authorities, request);

            AUDIT.info("GATEWAY AUTH SUCCESS userId={} ip={} requestId{} uri={}",
                    userId, clientIp, requestId, request.getRequestURI());

            filterChain.doFilter(request, response);
        } catch (InvalidGatewaySecretException e){
            AUDIT.warn("GATEWAY_BYPASS_ATTEMPT ip={} requestId={} uri={}",
                    clientIp, requestId, request.getRequestURI());
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        } catch (MissingIdentityHeaderException e){
            log.error("Gateway secret valid but identity header is missing - gateway misconfiguration. " +
                    "requestId={} missingHeader={}", requestId, e.getMessage());
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
        } catch (AuthorityResolutionException e){
            // Issues with IAM (AuthMat)
            log.error("Authority resolution failed for request. requestId={} reason={}",
                    requestId, e.getMessage());
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service temporarily unavailable");
        } catch (Exception e){
            log.error("Unexpected error in GatewayIdentityFilter requestId={}", requestId, e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }finally {
            MDC.clear();
        }

        filterChain.doFilter(request, response);
    }

    private void populateSecurityContext(
            String userId,
            Set<GrantedAuthority> authorities,
            HttpServletRequest request
    ){
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // Adds security metadata (IP, Session) to the authenticated user context
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        if(response.isCommitted()){
            log.warn("Response already committed, cannot write error. status={}", status);
            return;
        }
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "status", status,
                "error", message,
                "timestamp", Instant.now().toString()
        )));
        response.getWriter().flush(); // pushes whatever is written, to client immidietly
    }

    private String resolveRequestId(HttpServletRequest request){
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        return (requestId != null && !requestId.isBlank()) ?
                requestId.strip() : UUID.randomUUID().toString();
    }

    private String resolveClientIp(HttpServletRequest request){
        String clientIp = request.getHeader(FORWARDED_IP_HEADER);
        if(clientIp != null && !clientIp.isBlank()){
            return clientIp.split(",")[0].strip();
        }
        return request.getRemoteAddr();
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    /**
     * Compares a provided secret against the expected value using a constant-time
     * comparison to prevent timing attacks.
     *
     * <p><b>Why not use Arrays.equals()?</b></p>
     * A naive byte comparison (such as {@code Arrays.equals}) returns as soon as a
     * mismatch is found. This means the execution time varies depending on how many
     * leading bytes match between the two inputs.
     *
     * <p><b>Timing attack risk</b></p>
     * An attacker can exploit these timing differences by repeatedly sending guesses
     * and measuring how long the comparison takes. If a comparison takes slightly
     * longer, it may indicate that more bytes matched. Over many requests, this can
     * allow the attacker to infer the secret value one byte at a time.
     *
     * <p><b>Why MessageDigest.isEqual?</b></p>
     * {@link java.security.MessageDigest#isEqual(byte[], byte[])} performs a
     * constant-time comparison (or as close as practical), meaning it always
     * evaluates all bytes regardless of mismatches. This removes the timing signal
     * that attackers rely on and makes it significantly harder to extract the secret.
     *
     * <p><b>Summary</b></p>
     * This method ensures that secret comparisons do not leak information through
     * execution timing, helping protect against side-channel attacks.
     */
    private void verifyGatewaySecret(HttpServletRequest request){
        String gatewaySecret = request.getHeader(GATEWAY_SECRET_HEADER);

        if(gatewaySecret == null ||
                !MessageDigest.isEqual(gatewaySecret.getBytes(StandardCharsets.UTF_8), expectedSecretBytes)){
            throw new InvalidGatewaySecretException();
        }
    }

    static final class InvalidGatewaySecretException extends RuntimeException{
        InvalidGatewaySecretException(){super("Invalid or missing gateway secret");}
    }

    static final class MissingIdentityHeaderException extends RuntimeException{
        MissingIdentityHeaderException(String headerName){
            super("Missing identity header: " + headerName);
        }
    }

}