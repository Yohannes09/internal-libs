package com.payme.token.persistence;

import com.payme.token.model.PublicKeyRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Default in-memory implementation of the {@link PublicKeyHistory} interface.
 * <p>
 * This class maintains a thread-safe, fixed-size queue of recent public keys used for
 * token verification. It automatically trims older keys once the limit is reached,
 * ensuring memory safety while allowing clients to verify signatures during key rotation.
 * <p>
 * This implementation is suitable for applications that need a lightweight and
 * in-process solution for maintaining public key history.
 * <p>
 * Thread-safe and ready for use as a Spring-managed singleton.
 *
 * <p><strong>Configuration:</strong> The maximum number of keys retained is set
 * via constructor and should fall between 3 and 20 keys.
 *
 * <p><strong>Note:</strong> This implementation does not persist key history and will
 * reset on application restart.
 */
@Component
@Validated
@RequiredArgsConstructor
public class PublicKeyHistoryImp implements PublicKeyHistory{
    private static final Integer DEFAULT_TRACE_SIZE = 10;

    private final Integer maxKeysTraced;
    private final Deque<PublicKeyRecord> keyHistory;

    public PublicKeyHistoryImp(){
        this.maxKeysTraced = DEFAULT_TRACE_SIZE;
        this.keyHistory = new ConcurrentLinkedDeque<>();
    }


    /**
     * Adds a new {@link PublicKeyRecord} to the key history queue.
     * <p>
     * If the queue has reached its configured maximum size, the oldest
     * key is automatically evicted to make room for the new one.
     * <p>
     * The most recent key is always placed at the end of the queue.
     *
     * @param publicKeyRecord the public key metadata to store
     */
    @Override
    public void addKey(PublicKeyRecord publicKeyRecord) {
        if(publicKeyRecord == null){
            throw new NullPointerException("Public key history cannot contain null entries. ");
        }

        if(keyHistory.size() >= maxKeysTraced){
            keyHistory.pollFirst();
        }

        keyHistory.addLast(publicKeyRecord);
    }


    @Override
    public void addKeys(List<PublicKeyRecord> publicKeyRecords) {
        if(publicKeyRecords == null){
            throw new NullPointerException("Public key history cannot contain null entries. ");
        }

        if(publicKeyRecords.isEmpty()){
            throw new IllegalStateException("Must provide a list containing at least one object. ");
        }

        publicKeyRecords.forEach(this::addKey);
    }


    /**
     * Retrieves a reverse-chronological list of all retained public key records.
     * <p>
     * The list is sorted from most recent to oldest based on the
     * {@code createdAt} timestamp of each key. The result is a read-only snapshot
     * of the current in-memory history, useful for signature validation of tokens
     * issued with recently rotated keys.
     *
     * @return a sorted list of {@link PublicKeyRecord} entries, newest first
     */
    @Override
    public List<PublicKeyRecord> getKeyHistoryAscending() {
        return keyHistory.stream()
                .sorted(Comparator.comparing(PublicKeyRecord::getCreatedAt).reversed())
                .toList();
    }

}
