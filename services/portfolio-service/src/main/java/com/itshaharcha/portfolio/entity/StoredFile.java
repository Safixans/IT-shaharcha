package com.itshaharcha.portfolio.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * An uploaded file blob. Bytes are stored inline (small documents — certificates,
 * images); certificates and portfolio items reference the row by {@code id}.
 *
 * <p>Note: a plain {@code byte[]} maps to a PostgreSQL {@code bytea} column. {@code @Lob}
 * is deliberately avoided — on PostgreSQL it routes to the large-object (OID) API, which
 * does not match the {@code bytea} column in the migration.
 */
@Getter
@Setter
@Entity
@Table(name = "files")
public class StoredFile extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "data")
    private byte[] data;
}
