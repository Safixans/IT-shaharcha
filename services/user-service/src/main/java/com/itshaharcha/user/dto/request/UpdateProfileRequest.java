package com.itshaharcha.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(max = 150)
        String fullName,

        @Size(max = 512)
        String avatarUrl,

        @Size(max = 1000)
        String bio,

        @Size(max = 80)
        String country) {
}
