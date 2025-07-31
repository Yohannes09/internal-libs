package com.payme.token.persistence;

import com.payme.token.model.PublicKeyRecord;

public interface PublicKeyStore<T extends PublicKeyRecord> {
    T save(T t);
}
