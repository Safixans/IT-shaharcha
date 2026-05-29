package com.itshaharcha.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** External profile link; reused for both profile input and output. */
public record ProfileLinkDto(

        @NotBlank
        @Size(max = 80)
        String label,

        @NotBlank
        @Size(max = 512)
        String url) {
}
