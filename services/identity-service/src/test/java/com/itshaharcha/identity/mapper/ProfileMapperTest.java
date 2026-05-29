package com.itshaharcha.identity.mapper;

import com.itshaharcha.identity.dto.request.ProfileLinkDto;
import com.itshaharcha.identity.dto.response.ProfileResponse;
import com.itshaharcha.identity.entity.Profile;
import com.itshaharcha.identity.entity.ProfileLink;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileMapperTest {

    private final ProfileMapper mapper = new ProfileMapperImpl();

    @Test
    void toResponse_mapsProfileFieldsUsernameAndLinks() {
        Profile profile = new Profile();
        UUID accountId = UUID.randomUUID();
        profile.setAccountId(accountId);
        profile.setFullName("Jane Doe");
        profile.setBio("Learner");
        profile.setCountry("UZ");
        profile.getLinks().add(new ProfileLink("GitHub", "https://github.com/jane"));

        ProfileResponse response = mapper.toResponse(profile, "jane");

        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.username()).isEqualTo("jane");
        assertThat(response.fullName()).isEqualTo("Jane Doe");
        assertThat(response.country()).isEqualTo("UZ");
        assertThat(response.links()).hasSize(1);
        assertThat(response.links().get(0).label()).isEqualTo("GitHub");
        assertThat(response.links().get(0).url()).isEqualTo("https://github.com/jane");
    }

    @Test
    void toEntity_mapsDtoToEmbeddable() {
        ProfileLink link = mapper.toEntity(new ProfileLinkDto("LinkedIn", "https://linkedin.com/in/jane"));

        assertThat(link.getLabel()).isEqualTo("LinkedIn");
        assertThat(link.getUrl()).isEqualTo("https://linkedin.com/in/jane");
    }
}
