package com.itshaharcha.user.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

    /** References auth.accounts.id; the source of identity. Unique per account. */
    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "country", length = 80)
    private String country;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC")
    private List<EducationHistory> educationHistory = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("issuedAt DESC")
    private List<Certificate> certificates = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioItem> portfolioItems = new ArrayList<>();

    public void addCertificate(Certificate certificate) {
        certificate.setProfile(this);
        certificates.add(certificate);
    }

    public void addEducation(EducationHistory education) {
        education.setProfile(this);
        educationHistory.add(education);
    }

    public void addPortfolioItem(PortfolioItem item) {
        item.setProfile(this);
        portfolioItems.add(item);
    }
}
