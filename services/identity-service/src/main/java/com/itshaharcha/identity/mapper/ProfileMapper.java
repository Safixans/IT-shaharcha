package com.itshaharcha.identity.mapper;

import com.itshaharcha.identity.dto.request.ProfileLinkDto;
import com.itshaharcha.identity.dto.response.ProfileResponse;
import com.itshaharcha.identity.entity.Profile;
import com.itshaharcha.identity.entity.ProfileLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "accountId", source = "profile.accountId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "links", source = "profile.links")
    ProfileResponse toResponse(Profile profile, String username);

    ProfileLinkDto toDto(ProfileLink link);

    ProfileLink toEntity(ProfileLinkDto dto);
}
