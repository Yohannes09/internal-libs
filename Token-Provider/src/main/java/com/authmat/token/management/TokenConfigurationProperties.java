package com.payme.token.management;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Root configuration class for token settings.
 * <p>
 * This class maps all configurable token-related properties under the {@code token.*} prefix
 * from application properties or YAML files.
 * <p>
 * Required fields are validated on startup using JSR-380 (Bean Validation). Optional fields
 * are nullable and may assume default behavior if not provided.
 * <p>
 * Example configuration (YAML):
 * <pre>
 * token:
 *   signing:
 *     algorithm: RS256
 *     key-id: key1
 *     key-size: 2048
 *     rotation-interval-minutes: 60
 *   encoding:
 *     type: JWT
 *     compress: false
 *   validation:
 *     clock-skew-seconds: 60
 *   default-claims:
 *     audience: my-app
 *     issuer: my-company
 *   templates:
 *     access-token:
 *       validity-minutes: 15
 *   profiles:
 *     user:
 *       login:
 *         template: access-token
 * </pre>
 */
@Component("tokenConfiguration")
@ConfigurationProperties(prefix = "token")
@Getter
@Setter
@Validated
public class TokenConfigurationProperties {
    @Valid
    @NotNull
    private Signing signing;

    @Valid
    @NotNull
    private Encoding encoding;

    @Valid
    @NotNull
    private Validation validation;

    @Valid
    @NotNull
    private DefaultClaims defaultClaims;

    @Valid
    @NotEmpty(message = "Must have at least 1 token template {token.templates}")
    private Map<String, TokenTemplate> templates;

    @Valid
    @NotEmpty(message = "Must have at least 1 token profile {token.profiles}")
    private Map<String, Map<String, TokenTemplateReference>> profiles;


    /**
     * Configuration for signing tokens.
     * <p>
     * This section defines how tokens are cryptographically signed. You must provide the
     * algorithm, key ID, key size, and rotation interval.
     * <p>
     * The signing keys should be rotated periodically to enhance security. Rotation is
     * based on a time interval and should be managed automatically or externally.
     *
     * <ul>
     *   <li>{@code algorithm} – The signature algorithm (e.g., RS256, ES256)</li>
     *   <li>{@code key-id} – A unique identifier for the key used to sign the token</li>
     *   <li>{@code key-size} – Bit-length of the signing key (e.g., 2048 for RSA)</li>
     *   <li>{@code rotation-interval-minutes} – How often the signing key is rotated</li>
     * </ul>
     *
     * Example:
     * <pre>
     * token:
     *   signing:
     *     algorithm: RS256
     *     key-id: auth-key-1
     *     key-size: 2048
     *     rotation-interval-minutes: 60
     * </pre>
     */
    @Getter
    @Setter
    public static class Signing{
        @NotBlank(message = "Provide a valid signing algorithm. ")
        private String algorithm;

        @NotBlank(message = "Must provide an ID for the entity signing the token. ")
        private String keyId;

        @NotNull(message = "Must provide a value for field {signing.key-size} ")
        @Min(value = 2048, message = "Key size must be at least 2048 bits.")
        @Max(value = 5000, message = "Key size must not exceed 5000 bits.")
        private Integer keySize;

        @NotNull(message = "Must provide a value for field 'rotation-interval-minutes'. ")
        @Min(value = 30, message = "Signing key must be rotated at least every thirty minutes. ")
        private Integer rotationIntervalMinutes;

    }


    /**
     * Configuration for token encoding format.
     * <p>
     * Determines the structural and optional compression format of the generated tokens.
     * <p>
     * This section is useful when supporting different token types (e.g., JWT, Paseto)
     * or when optimizing for size with compression.
     *
     * <ul>
     *   <li>{@code type} – The encoding type (e.g., JWT, Paseto)</li>
     *   <li>{@code compress} – Whether to compress the token payload (e.g., using DEFLATE)</li>
     * </ul>
     *
     * Example:
     * <pre>
     * token:
     *   encoding:
     *     type: JWT
     *     compress: false
     * </pre>
     */
    @Getter
    @Setter
    public static class Encoding{
        @NotBlank(message = "Must provide a value for field {encoding.type} ")
        private String type;

        private boolean compress;
    }


    /**
     * Configuration for token validation behavior.
     * <p>
     * Defines parameters that affect how token validity is evaluated, particularly
     * regarding time-based claims like expiration and not-before.
     *
     * <ul>
     *   <li>{@code clock-skew-seconds} – Number of seconds allowed as leeway when
     *   comparing token timestamps to the system clock. Useful to accommodate small
     *   clock differences between systems.</li>
     * </ul>
     *
     * Example:
     * <pre>
     * token:
     *   validation:
     *     clock-skew-seconds: 60
     * </pre>
     */
    @Getter
    @Setter
    public static class Validation{
        private Integer clockSkewSeconds;
    }


    /**
     * Configuration for default token claims.
     * <p>
     * These claims are automatically embedded into every token unless overridden by
     * templates or profiles. They help identify the intended recipient and issuer
     * of the token.
     *
     * <ul>
     *   <li>{@code audience} – The target audience for the token (e.g., service name, API)</li>
     *   <li>{@code issuer} – The entity that issued the token (e.g., your auth server)</li>
     * </ul>
     *
     * Example:
     * <pre>
     * token:
     *   default-claims:
     *     audience: my-service
     *     issuer: my-auth-server
     * </pre>
     */
    @Getter
    @Setter
    public static class DefaultClaims{
        @NotBlank(message = "Must provide a value for field {default-claims.audience}")
        private String audience;

        @NotBlank(message = "Must provide a value for field 'rotation-interval-minutes'. ")
        private String issuer;
    }


    /**
     * Defines the validity settings for a specific token template.
     * <p>
     * Templates are reusable configuration blocks that determine how long a token
     * issued under that template remains valid. Templates are referenced by
     * profiles to apply consistent behavior across token use cases.
     *
     * <ul>
     *   <li>{@code validity-minutes} – Number of minutes the token remains valid from issuance</li>
     * </ul>
     *
     * Example:
     * <pre>
     * token:
     *   templates:
     *     access-token:
     *       validity-minutes: 15
     * </pre>
     */
    @Getter
    @Setter
    public static class TokenTemplate{
        @NotNull(message = "Must provide a value for field {token-template.validity-minutes}")
        @Min(value = 1, message = "Tokens must have at least 1 minute of validity. ")
        private Integer validityMinutes;
    }


    /**
     * A reference to a predefined token template.
     * <p>
     * Profiles use these references to associate specific use cases (e.g., login, refresh)
     * with a template that defines token behavior like validity. The referenced template
     * must exist in the {@code token.templates} section.
     *
     * <ul>
     *   <li>{@code template} – The name of the template to use for this profile action</li>
     * </ul>
     *
     * Example:
     * <pre>
     * token:
     *   profiles:
     *     user:
     *       login:
     *         template: access-token
     * </pre>
     */
    @Getter
    @Setter
    public static class TokenTemplateReference{
        private String template;
    }

    @PostConstruct
    public void validateProfiles(){
        boolean containsInvalidReference;

        if(!profiles.isEmpty()){
             containsInvalidReference = profiles.entrySet().stream()
                    .flatMap(profileEntry -> profileEntry.getValue().values().stream())
                    .anyMatch(reference -> !templates.containsKey(reference.getTemplate()));

            if (containsInvalidReference) {
                throw new IllegalStateException("Entered invalid reference within the profiles. ");
            }
        }

    }

}
