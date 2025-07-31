package com.authmat.token.signing;

import com.authmat.token.config.TokenConfigurationProperties;
import com.authmat.token.model.PublicKeyMetaData;
import com.authmat.token.model.PublicKeyMetaDataImp;
import com.authmat.token.persistence.PublicKeyHistory;
import com.payme.token.exception.KeyInitializationException;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.*;
import java.util.*;


/**
 * Responsibilities:
 * <ul>
 *     <li>Generates and rotates the key pair used for signing and validating tokens.</li>
 *     <li>Persists the public key and tracks historical keys for verification support.</li>
 *     <li>Provides access to the currently active signing key and metadata.</li>
 * </ul>
 * <p>
 * The manager is designed to be thread-safe and supports pluggable key formats via {@link KeyFactory}.
 */

@Slf4j
@RequiredArgsConstructor
public class SigningKeyManagerInternal implements SigningKeyManager{
    private static final int MINIMUM_KEY_SIZE_BITS = 2048;

    private final TokenConfigurationProperties configuration;
    private final PublicKeyHistory publicKeyHistory;
    private volatile ActiveKeyPair activeKeyPair;


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
        rotateSigningKey();
        log.info("Successfully rotated Signing Key. ");
    }


    public void manualKeyRotation(){
        rotateSigningKey();
        log.info("Successful manual Signing Key rotation. ");
    }




    /**
     * @return an immutable {@link List} of base64-encoded public keys.
     * @throws KeyInitializationException if no signing key has been initialized.
     */
    public List<PublicKeyMetaData> getPublicKeyHistory(){
        validateKeyIsInitialized();
        return Collections.unmodifiableList(publicKeyHistory.getKeyHistoryAscending());
    }


    /**
     * @return the active {@link PrivateKey} used for signing.
     * @throws KeyInitializationException if the signing key has not been initialized.
     */
    public PrivateKey getActiveSigningKey(){
        validateKeyIsInitialized();
        return activeKeyPair.privateKey();
    }


    /**
     * @return the current active public key record of type {@code T}.
     * @throws KeyInitializationException if the public key has not been initialized.
     */
    public PublicKeyMetaData getActivePublicKey(){
        validateKeyIsInitialized();
        return activeKeyPair.publicKeyMetaData();
    }


    private synchronized void rotateSigningKey(){
        try {
            this.activeKeyPair = null;
            String signingAlgorithm = configuration.getSigning().getAlgorithm();

            KeyPair keyPair = generateKeyPair(
                    configuration.getSigning().getKeySize(),
                    signingAlgorithm
            );

            String encodedPublicKey = base64Encode(keyPair.getPublic());

            PublicKeyMetaData publicKey = new PublicKeyMetaDataImp(encodedPublicKey, signingAlgorithm);
            this.activeKeyPair = new ActiveKeyPair(publicKey, keyPair.getPrivate());

            publicKeyHistory.addKey(publicKey);

        } catch (Exception e) {
            log.error("Failed to rotate signing key. {}", e.getMessage(), e);
            throw new KeyInitializationException(STR."Failed to rotate signing key. \{e.getMessage()}");
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
     * @param publicKey the public key to encode.
     * @return the base64-encoded string representation of the public key.
     */
    private String base64Encode(@NotNull PublicKey publicKey){
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }


    private void validateKeyIsInitialized(){
        if(activeKeyPair == null || publicKeyHistory.getKeyHistoryAscending().isEmpty()){
            log.error("ERROR no active Signing Key initialized. ");
            throw new KeyInitializationException("Failed to initialize public key. ");
        }
    }


    /**
    * Holds the active signing key pair, including public key metadata and the private key.
    */
    private record ActiveKeyPair(
            PublicKeyMetaData publicKeyMetaData, PrivateKey privateKey
    ) {

    }

}

