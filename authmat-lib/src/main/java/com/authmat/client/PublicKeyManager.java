package com.authmat.client;

import com.authmat.events.PublicKeyRotationEvent;
import com.authmat.model.publickey.PublicKeyMetadata;
import com.authmat.model.publickey.PublicKeyMetadataImp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@RequiredArgsConstructor
@Slf4j
public class PublicKeyManager {
    private final int maxKeysTraced;
    private final Deque<String> publicKeyInsertionSequence = new ConcurrentLinkedDeque<>();
    private final Map<String, PublicKeyMetadata> publicKeyRepository = new ConcurrentHashMap<>();


    public void addKey(PublicKeyRotationEvent event){
        if(publicKeyInsertionSequence.size() >= maxKeysTraced){
            String key = publicKeyInsertionSequence.poll();
            publicKeyRepository.remove(key);
        }

        publicKeyInsertionSequence.addLast(
                event.kid());

        publicKeyRepository.put(
                event.kid(),
                new PublicKeyMetadataImp(
                        event.publicKey(), event.signingKeyAlgorithm(), event.jwtAlgorithm()));
    }

    public Collection<String> getKeyMetadata(){
        return Collections.unmodifiableCollection(publicKeyInsertionSequence);
    }

    public Optional<PublicKeyMetadata> findKeyByKid(String kid){
        return Optional.of(publicKeyRepository.getOrDefault(kid, null));
    }

}
