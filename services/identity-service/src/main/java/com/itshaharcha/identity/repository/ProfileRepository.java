package com.itshaharcha.identity.repository;

import com.itshaharcha.identity.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByAccountId(UUID accountId);

    boolean existsByAccountId(UUID accountId);
}
