package com.itshaharcha.identity.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SetRolesRequest(

        @NotNull
        List<String> roles) {
}
