package com.itshaharcha.identity.mapper;

import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    AccountResponse toResponse(Account account);

    @Named("rolesToNames")
    default List<String> rolesToNames(Set<Role> roles) {
        return roles.stream().map(Role::getName).sorted().collect(Collectors.toList());
    }
}
