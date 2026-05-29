package com.itshaharcha.user.mapper;

import com.itshaharcha.user.dto.response.CertificateResponse;
import com.itshaharcha.user.dto.response.EducationResponse;
import com.itshaharcha.user.dto.response.PortfolioItemResponse;
import com.itshaharcha.user.dto.response.ProfileResponse;
import com.itshaharcha.user.entity.Certificate;
import com.itshaharcha.user.entity.EducationHistory;
import com.itshaharcha.user.entity.PortfolioItem;
import com.itshaharcha.user.entity.PortfolioItemType;
import com.itshaharcha.user.entity.Profile;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileMapperTest {

    private final ProfileMapper mapper = new ProfileMapperImpl();

    @Test
    void toResponse_mapsProfileWithNestedCollections() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setAccountId(UUID.randomUUID());
        profile.setFullName("Jane");
        profile.setBio("hello");
        profile.setCountry("UZ");

        Certificate cert = new Certificate();
        cert.setId(UUID.randomUUID());
        cert.setTitle("AWS");
        profile.addCertificate(cert);

        EducationHistory edu = new EducationHistory();
        edu.setId(UUID.randomUUID());
        edu.setInstitution("MIT");
        profile.addEducation(edu);

        PortfolioItem item = new PortfolioItem();
        item.setId(UUID.randomUUID());
        item.setType(PortfolioItemType.PROJECT);
        item.setTitle("My App");
        profile.addPortfolioItem(item);

        ProfileResponse response = mapper.toResponse(profile);

        assertThat(response.id()).isEqualTo(profile.getId());
        assertThat(response.fullName()).isEqualTo("Jane");
        assertThat(response.bio()).isEqualTo("hello");
        assertThat(response.country()).isEqualTo("UZ");
        assertThat(response.certificates()).hasSize(1);
        assertThat(response.certificates().get(0).title()).isEqualTo("AWS");
        assertThat(response.educationHistory()).hasSize(1);
        assertThat(response.educationHistory().get(0).institution()).isEqualTo("MIT");
        assertThat(response.portfolioItems()).hasSize(1);
        assertThat(response.portfolioItems().get(0).title()).isEqualTo("My App");
    }

    @Test
    void toResponse_mapsCertificate() {
        Certificate cert = new Certificate();
        cert.setId(UUID.randomUUID());
        cert.setTitle("AWS SA");
        cert.setIssuer("Amazon");
        cert.setIssuedAt(LocalDate.of(2024, 1, 1));

        CertificateResponse response = mapper.toResponse(cert);
        assertThat(response.title()).isEqualTo("AWS SA");
        assertThat(response.issuer()).isEqualTo("Amazon");
        assertThat(response.issuedAt()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void toResponse_mapsEducation() {
        EducationHistory edu = new EducationHistory();
        edu.setId(UUID.randomUUID());
        edu.setInstitution("MIT");
        edu.setDegree("BSc");
        edu.setFieldOfStudy("CS");

        EducationResponse response = mapper.toResponse(edu);
        assertThat(response.institution()).isEqualTo("MIT");
        assertThat(response.degree()).isEqualTo("BSc");
        assertThat(response.fieldOfStudy()).isEqualTo("CS");
    }

    @Test
    void toResponse_mapsPortfolioItem() {
        PortfolioItem item = new PortfolioItem();
        item.setId(UUID.randomUUID());
        item.setType(PortfolioItemType.PROJECT);
        item.setTitle("My App");
        item.setUrl("http://example.com");

        PortfolioItemResponse response = mapper.toResponse(item);
        assertThat(response.type()).isEqualTo(PortfolioItemType.PROJECT);
        assertThat(response.title()).isEqualTo("My App");
        assertThat(response.url()).isEqualTo("http://example.com");
    }

    @Test
    void toResponse_nullProfile_returnsNull() {
        assertThat(mapper.toResponse((Profile) null)).isNull();
    }

    @Test
    void toResponse_nullCollections_yieldNullLists() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setEducationHistory(null);
        profile.setCertificates(null);
        profile.setPortfolioItems(null);

        ProfileResponse response = mapper.toResponse(profile);

        assertThat(response.educationHistory()).isNull();
        assertThat(response.certificates()).isNull();
        assertThat(response.portfolioItems()).isNull();
    }

    @Test
    void toResponse_nullNestedEntities_returnNull() {
        assertThat(mapper.toResponse((Certificate) null)).isNull();
        assertThat(mapper.toResponse((EducationHistory) null)).isNull();
        assertThat(mapper.toResponse((PortfolioItem) null)).isNull();
    }
}
