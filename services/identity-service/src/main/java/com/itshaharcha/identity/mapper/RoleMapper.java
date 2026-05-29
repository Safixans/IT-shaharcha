package com.itshaharcha.identity.mapper;

import com.itshaharcha.identity.dto.response.RoleResponse;
import com.itshaharcha.identity.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);
}
