package com.itshaharcha.auth.mapper;

import com.itshaharcha.auth.dto.response.AccountResponse;
import com.itshaharcha.auth.entity.Account;
import com.itshaharcha.auth.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @org.mapstruct.Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    AccountResponse toResponse(Account account);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        return roles.stream().map(r -> r.getName().name()).collect(Collectors.toSet());
    }
}
