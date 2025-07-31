package com.authmat.token.persistence;

import com.authmat.token.model.PublicKeyMetaDataJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PublicKeyMetaDataRepository extends JpaRepository<PublicKeyMetaDataJpa, UUID> {
}
