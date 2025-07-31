package com.payme.token.management.secured;

import com.payme.token.management.TokenConfigurationProperties;
import com.payme.token.model.PublicKeyRecord;
import com.payme.token.persistence.PublicKeyHistory;
import com.payme.token.persistence.PublicKeyStore;
import com.payme.token.exception.KeyInitializationException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.*;
import java.time.LocalDateTime;
import java.util.*;


/**
 * Manages the signing keys used for token generation.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Generates and rotates the key pair used for signing and validating tokens.</li>
 *     <li>Persists the public key and tracks historical keys for verification support.</li>
 *     <li>Provides access to the currently active signing key and metadata.</li>
 * </ul>
 * <p>
 * The manager is designed to be thread-safe and supports pluggable key formats via {@link KeyFactory}.
 *
 * @param <T> The concrete type of {@link PublicKeyRecord} used to store public key metadata.
 */

@Slf4j
@RequiredArgsConstructor
public class SigningKeyManager <T extends PublicKeyRecord>{
    private static final int MINIMUM_KEY_SIZE_BITS = 2048;

    private final PublicKeyStore<T> publicKeyStore;
    private final TokenConfigurationProperties configuration;
    private final PublicKeyHistory publicKeyHistory;
    private final KeyFactory<T> keyFactory;

    private volatile ActiveKeyPair activeKeyPair;


    /**
     * Returns a read-only snapshot of recent public keys used for token signing.
     * <p>
     * Includes the current public key and previously used keys to allow clients
     * to verify tokens issued before a key rotation.
     *
     * @return an immutable {@link List} of base64-encoded public keys.
     * @throws KeyInitializationException if no signing key has been initialized.
     */
    public List<PublicKeyRecord> getPublicKeyHistory(){
        validateKeyIsInitialized();
        return Collections.unmodifiableList(publicKeyHistory.getKeyHistoryAscending());
    }


    /**
     * Retrieves the current private key used for signing tokens.
     * <p>
     * This key must be kept secure and is used internally to sign authentication tokens.
     *
     * @return the active {@link PrivateKey} used for signing.
     * @throws KeyInitializationException if the signing key has not been initialized.
     */
    protected PrivateKey getActiveSigningKey(){
        validateKeyIsInitialized();
        return activeKeyPair.getPrivateKey();
    }


    /**
     * Returns the active public key metadata record.
     * <p>
     * This key corresponds to the private signing key and is shared with clients
     * to verify signatures during key rotation periods.
     *
     * @return the current active public key record of type {@code T}.
     * @throws KeyInitializationException if the public key has not been initialized.
     */
    public T getActivePublicKey(){
        validateKeyIsInitialized();
        return activeKeyPair.getPublicKeyRecord();
    }


    public void manualKeyRotation(){
        this.activeKeyPair = null;
        rotateSigningKey();
        log.info("Successful manual Signing Key rotation. ");
    }


    @PostConstruct
    public void initializeKey(){
        rotateSigningKey();
        log.info("Successfully initialized Signing Key. ");
    }


    /**
     * Scheduled task to rotate signing keys at a fixed interval.
     * Interval is configured in application properties via:
     * {@code token.signing.rotation-interval-minutes}
     */
    @Scheduled(
            fixedRateString = "#{@tokenConfiguration.signing.rotationIntervalMinutes * 60 * 1000}",
            fixedDelayString = "#{@tokenConfiguration.signing.rotationIntervalMinutes * 60 * 1000}"
    )
    public void scheduledKeyRotation(){
        this.activeKeyPair = null;
        rotateSigningKey();
        log.info("Successfully rotated Signing Key. ");
    }


    /**
     * Manages the signing keys used for token generation.
     * <p>
     * Responsibilities:
     * <ul>
     *     <li>Generates and rotates the key pair used for signing and validating tokens.</li>
     *     <li>Persists the public key and tracks historical keys for verification support.</li>
     *     <li>Provides access to the currently active signing key and metadata.</li>
     * </ul>
     * <p>
     */
    private synchronized void rotateSigningKey(){
        try {
            KeyPair keyPair = generateKeyPair(
                    configuration.getSigning().getKeySize(),
                    configuration.getSigning().getAlgorithm()
            );

            String publicKey = base64Encode(keyPair.getPublic());
            String signingAlgorithm = configuration.getSigning().getAlgorithm();

            T keyRecord = persistPublicKey(publicKey, signingAlgorithm);
            this.activeKeyPair = new ActiveKeyPair(keyRecord, keyPair.getPrivate());
            publicKeyHistory.addKey(keyRecord);

        } catch (Exception e) {
            log.error("Failed to rotate signing key. {}", e.getMessage(), e);
            throw new KeyInitializationException("Failed to rotate signing key. " + e.getMessage());
        }

    }


    private KeyPair generateKeyPair(int sizeBits, String algorithmType) throws NoSuchAlgorithmException {
        if(sizeBits < MINIMUM_KEY_SIZE_BITS){
            throw new KeyInitializationException("Key size must be at least 2048 bits for security reasons. ");
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithmType);
        keyPairGenerator.initialize(sizeBits);

        return keyPairGenerator.generateKeyPair();
    }


    /**
     * Creates and persists a new public key record with issued and expiry timestamps.
     *
     * @param publicKey base64-encoded public key string.
     * @param signatureAlgorithm the algorithm used for signing keys.
     * @return the persisted key record of type {@code T}.
     */
    private T persistPublicKey(String publicKey, String signatureAlgorithm){
        int keyRotationIntervalMinutes = configuration
                .getSigning()
                .getRotationIntervalMinutes();

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(keyRotationIntervalMinutes);

        return publicKeyStore.save(
                keyFactory.create(publicKey, signatureAlgorithm, issuedAt, expiresAt)
        );

    }


    /**
     * Encodes the given {@link PublicKey} into a base64 string.
     *
     * @param publicKey the public key to encode.
     * @return the base64-encoded string representation of the public key.
     */
    private String base64Encode(PublicKey publicKey){
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }


    /**
     * Validates that the signing key and public key history have been initialized.
     * Throws {@link KeyInitializationException} if validation fails.
     */
    private void validateKeyIsInitialized(){
        if(activeKeyPair == null || publicKeyHistory.getKeyHistoryAscending().isEmpty()){
            log.error("ERROR no active Signing Key initialized. ");
            throw new KeyInitializationException("Failed to initialize public key. ");
        }
    }


    /**
     * Holds the active signing key pair, including public key metadata and the private key.
     */
    @Getter
    @RequiredArgsConstructor
    private class ActiveKeyPair{
        private final T publicKeyRecord;
        private final PrivateKey privateKey;
    }


    /**
     * Functional interface to create a key record instance with metadata.
     *
     * @param <T> the type of {@link PublicKeyRecord}.
     */
    @FunctionalInterface
    public interface KeyFactory<T> {
        T create(
                String publicKey,
                String signingAlgorithm,
                LocalDateTime issuedAt,
                LocalDateTime expiresAt
        );

    }


}

