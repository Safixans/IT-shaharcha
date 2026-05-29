package com.itshaharcha.identity.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {

    /** References accounts.id; one profile per account. */
    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "locale", length = 16)
    private String locale;

    @Column(name = "country", length = 80)
    private String country;

    @ElementCollection
    @CollectionTable(name = "profile_links", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    private List<ProfileLink> links = new ArrayList<>();
}
