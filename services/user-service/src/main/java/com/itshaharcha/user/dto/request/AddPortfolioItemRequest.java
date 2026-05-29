package com.itshaharcha.user.dto.request;

import com.itshaharcha.user.entity.PortfolioItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddPortfolioItemRequest(

        @NotNull
        PortfolioItemType type,

        @NotBlank @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        @Size(max = 512)
        String url) {
}
