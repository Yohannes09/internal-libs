package com.payme.token.persistence;

import com.payme.token.model.PublicKeyRecord;
import com.payme.token.model.PublicKeyRecordJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SigningKeyHistoryTest {
    private static final int MAX_PK_HISTORY_SIZE = 3;

    private PublicKeyHistory publicKeyHistory;


    @BeforeEach
    void setUp(){
        publicKeyHistory = new PublicKeyHistoryImp(
                MAX_PK_HISTORY_SIZE,
                new ConcurrentLinkedDeque<>()
        );
    }


    @Test
    void addKey_StoresKeys(){
        publicKeyHistory.addKey(testData());

        List<PublicKeyRecord> history = publicKeyHistory.getKeyHistoryAscending();

        assertEquals(1, history.size());
        assertEquals("PK1", history.getFirst().getPublicKey());
    }


    @Test
    void addKey_RemovesFirstKeyWhenFull(){
        // (Head) PK1, PK2, PK3 (tail)
        publicKeyHistory.addKeys(testDataList());

        // (Head) PK2, PK3, PK4 (tail)
        publicKeyHistory.addKey(PublicKeyRecordJpa.builder().id(UUID.randomUUID()).publicKey("PK4").createdAt(LocalDateTime.now()).build());

        // return PK4, PK3, PK2
        List<PublicKeyRecord> publicKeys = publicKeyHistory.getKeyHistoryAscending();

        assertEquals(3, publicKeys.size());
        assertEquals("PK4", publicKeys.getFirst().getPublicKey());
        assertEquals("PK2", publicKeys.getLast().getPublicKey());
    }


    @Test
    void addKey_ThrowsExceptionOnNull(){
        assertThrows(NullPointerException.class, () -> publicKeyHistory.addKey(null));
    }


    @Test
    void addKeys_ThrowsExceptionOnNull(){
        assertThrows(NullPointerException.class, () -> publicKeyHistory.addKeys(null));
    }


    @Test
    void addKeys_ThrowsExceptionOnEmptyList(){
        assertThrows(IllegalStateException.class, () -> publicKeyHistory.addKeys(List.of()));
    }


    @Test
    void getkeyHistory_ReturnsCorrectChronologicalOrder(){
        publicKeyHistory.addKeys(testDataList());

        List<PublicKeyRecord> publicKeys = publicKeyHistory.getKeyHistoryAscending();

        assertEquals("PK3", publicKeys.getFirst().getPublicKey());
        assertEquals("PK1", publicKeys.getLast().getPublicKey());
    }


    PublicKeyRecord testData(){
        return PublicKeyRecordJpa.builder().id(UUID.randomUUID()).publicKey("PK1").createdAt(LocalDateTime.now()).build();
    }

    List<PublicKeyRecord> testDataList(){
        return List.of(
                PublicKeyRecordJpa.builder().id(UUID.randomUUID()).publicKey("PK1").createdAt(LocalDateTime.now()).build(),
                PublicKeyRecordJpa.builder().id(UUID.randomUUID()).publicKey("PK2").createdAt(LocalDateTime.now()).build(),
                PublicKeyRecordJpa.builder().id(UUID.randomUUID()).publicKey("PK3").createdAt(LocalDateTime.now()).build()
        );
    }

}
