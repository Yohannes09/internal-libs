package com.payme.token.persistence;

import com.payme.token.model.PublicKeyRecord;
import com.payme.token.model.PublicKeyRecordJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PublicKeyStoreJpa extends
        JpaRepository<PublicKeyRecordJpa, UUID>, PublicKeyStore<PublicKeyRecord> {

}
