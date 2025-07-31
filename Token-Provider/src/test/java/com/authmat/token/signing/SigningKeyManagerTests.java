//package com.payme.token.management.secured;
//
//import com.payme.token.exception.KeyInitializationException;
//import com.authmat.token.config.TokenConfigurationProperties;
//import com.payme.token.model.PublicKeyMetaData;
//import com.payme.token.model.PublicKeyMetaDataJpa;
//import com.payme.token.persistence.PublicKeyHistory;
//import com.payme.token.persistence.PublicKeyHistoryImp;
//import com.payme.token.persistence.PublicKeyStoreJpa;
//import com.payme.token.persistence.PublicKeyStore;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@Slf4j
//public class SigningKeyManagerTests {
//    private PublicKeyStore<PublicKeyMetaDataJpa> publicKeyStore;
//    private SigningKeyManagerInternal.KeyFactory<PublicKeyMetaData> keyFactory;
//    private TokenConfigurationProperties tokenConfigurationProperties;
//    private TokenConfigurationProperties.Signing signing;
//    private PublicKeyHistory publicKeyHistory;
//    private SigningKeyManagerInternal<PublicKeyMetaData> signingKeyManager;
//
//    @BeforeEach
//    void setUp(){
//        log.info("Beginning setup. ");
//
//        publicKeyStore = mock(PublicKeyStoreJpa.class);
//        keyFactory = mock(SigningKeyManagerInternal.KeyFactory.class);
//        tokenConfigurationProperties = mock(TokenConfigurationProperties.class);
//        signing = mock(TokenConfigurationProperties.Signing.class);
//        publicKeyHistory = mock(PublicKeyHistoryImp.class);
//
//        signingKeyManager = new SigningKeyManagerInternal<>(
//                publicKeyStore, tokenConfigurationProperties, publicKeyHistory, keyFactory
//        );
//
//        when(tokenConfigurationProperties.getSigning()).thenReturn(signing);
//        when(signing.getAlgorithm()).thenReturn("RSA");
//        when(signing.getKeySize()).thenReturn(2048);
//    }
//
//    @Test
//    void initializeKey_SetsSigningKey(){
//        log.info("Running initializeKey_SetsSigningKey()");
//        PublicKeyMetaData publicKeyRecord = mock(PublicKeyMetaDataJpa.class);
//
//        when(keyFactory.create(any(), any(), any(), any()))
//                .thenReturn(publicKeyRecord);
//        when(publicKeyStore.save(any())).thenReturn(publicKeyRecord);
//        when(publicKeyHistory.getKeyHistoryAscending()).thenReturn(List.of(publicKeyRecord));
//
//        signingKeyManager.initializeKey();
//
//        assertNotNull(signingKeyManager.getActivePublicKey());
//        assertEquals(1, signingKeyManager.getPublicKeyHistory().size());
//    }
//
//    @Test
//    void ScheduledKeyRotation_StoresPreviousKeys(){
//        log.info("Running ScheduledKeyRotation_StoresPreviousKeys()");
//        PublicKeyMetaData pk1 = mock(PublicKeyMetaDataJpa.class);
//        PublicKeyMetaData pk2 = mock(PublicKeyMetaDataJpa.class);
//
//        when(keyFactory.create(any(), any(), any(), any()))
//                .thenReturn(pk1)
//                .thenReturn(pk2);
//
//        when(publicKeyStore.save(any()))
//                .thenReturn(pk1)
//                .thenReturn(pk2);
//
//        when(publicKeyHistory.getKeyHistoryAscending())
//                .thenReturn(List.of(pk1, pk2));
//
//        signingKeyManager.initializeKey();
//        signingKeyManager.scheduledKeyRotation();
//
//        verify(publicKeyStore, times(2)).save(any());
//        assertEquals(2, signingKeyManager.getPublicKeyHistory().size());
//    }
//
//    @Test
//    void MultipleKeyRotations_StorePreviousKeys(){
//        log.info("Running MultipleKeyRotations_StorePreviousKeys()");
//
//        PublicKeyMetaData pk1 = mock(PublicKeyMetaDataJpa.class);
//        PublicKeyMetaData pk2 = mock(PublicKeyMetaDataJpa.class);
//        PublicKeyMetaData pk3 = mock(PublicKeyMetaDataJpa.class);
//
//        when(keyFactory.create(any(), any(), any(), any()))
//                .thenReturn(pk1)
//                .thenReturn(pk2)
//                .thenReturn(pk3);
//
//        when(publicKeyStore.save(any()))
//                .thenReturn(pk1)
//                .thenReturn(pk2)
//                .thenReturn(pk3);
//
//        when(publicKeyHistory.getKeyHistoryAscending())
//                .thenReturn(List.of(pk1, pk2, pk3));
//
//        signingKeyManager.initializeKey();
//        signingKeyManager.scheduledKeyRotation();
//        signingKeyManager.manualKeyRotation();
//
//        verify(publicKeyStore, times(3)).save(any());
//        assertEquals(3, signingKeyManager.getPublicKeyHistory().size());
//    }
//
//    @Test
//    void invalidAlgorithmThrowsException(){
//        log.info("Running invalidAlgorithmThrowsException()");
//
//        when(tokenConfigurationProperties.getSigning()).thenReturn(signing);
//        when(signing.getAlgorithm()).thenReturn("algorithm");
//        when(signing.getKeySize()).thenReturn(2047);
//
//        assertThrows(KeyInitializationException.class, () -> signingKeyManager.initializeKey());
//    }
//
//    @Test
//    void insufficientKeySizeThrowsException(){
//        log.info("Running insufficientKeySizeThrowsException()");
//
//        when(tokenConfigurationProperties.getSigning()).thenReturn(signing);
//        when(signing.getAlgorithm()).thenReturn("RSA");
//        when(signing.getKeySize()).thenReturn(2047);
//
//        assertThrows(KeyInitializationException.class, () -> signingKeyManager.initializeKey());
//    }
//
//}
