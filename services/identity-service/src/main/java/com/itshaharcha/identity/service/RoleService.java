package com.itshaharcha.identity.service;

import com.itshaharcha.identity.dto.request.RoleInput;
import com.itshaharcha.identity.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> list();

    RoleResponse create(RoleInput request);

    void delete(String roleName);
}
