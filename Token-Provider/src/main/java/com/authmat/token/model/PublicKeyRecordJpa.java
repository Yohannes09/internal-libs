package com.payme.token.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Entity representing a record of a public key used for JWT signature verification.
 * Each record includes the public key string, its associated signature algorithm,
 * timestamps for creation, expiration, and optional revocation.
 *
 * <p>This entity is persisted in the {@code public_key_record} table and supports
 * tracking of key lifecycle for secure token validation.</p>
 */
@Entity
@Table(name = "public_key_record")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyRecordJpa implements PublicKeyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Lob
    @Column(nullable = false, updatable = false, unique = true)
    private String publicKey;

    @Column(nullable = false, updatable = false)
    private String signatureAlgorithm;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;
}