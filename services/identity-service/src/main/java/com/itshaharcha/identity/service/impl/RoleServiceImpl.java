package com.itshaharcha.identity.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.identity.dto.request.RoleInput;
import com.itshaharcha.identity.dto.response.RoleResponse;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.mapper.RoleMapper;
import com.itshaharcha.identity.repository.RoleRepository;
import com.itshaharcha.identity.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAll(Sort.by("name")).stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoleResponse create(RoleInput request) {
        if (roleRepository.existsByName(request.name())) {
            throw ApplicationException.conflict("Role already exists: " + request.name());
        }
        Role saved = roleRepository.save(new Role(request.name(), request.description()));
        log.info("Created role {}", saved.getName());
        return roleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> ApplicationException.notFound("Role not found: " + roleName));
        roleRepository.delete(role);
        log.info("Deleted role {}", roleName);
    }
}
