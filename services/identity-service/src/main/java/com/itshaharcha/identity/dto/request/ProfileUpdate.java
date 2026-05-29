package com.itshaharcha.identity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Partial profile update — only non-null fields are applied. */
public record ProfileUpdate(

        @Size(max = 120)
        String fullName,

        @Size(max = 500)
        String bio,

        @Size(max = 512)
        String avatarUrl,

        @Size(max = 16)
        String locale,

        @Size(max = 80)
        String country,

        @Valid
        List<ProfileLinkDto> links) {
}
