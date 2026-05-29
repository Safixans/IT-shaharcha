package com.itshaharcha.user.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "certificates")
public class Certificate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "issuer", length = 200)
    private String issuer;

    /** References a stored object in file-storage-service. */
    @Column(name = "file_id")
    private UUID fileId;

    @Column(name = "credential_url", length = 512)
    private String credentialUrl;

    @Column(name = "issued_at")
    private LocalDate issuedAt;
}
