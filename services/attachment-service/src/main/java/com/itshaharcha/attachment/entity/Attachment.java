package com.itshaharcha.attachment.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Metadata for an object stored in MinIO. The bytes live in the bucket under {@code objectKey}. */
@Getter
@Setter
@Entity
@Table(name = "attachments")
public class Attachment extends BaseEntity {

    @Column(name = "owner_account_id", nullable = false)
    private UUID ownerAccountId;

    @Column(name = "object_key", nullable = false, unique = true)
    private String objectKey;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
}
