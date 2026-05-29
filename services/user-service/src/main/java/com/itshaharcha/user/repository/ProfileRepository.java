package com.itshaharcha.user.repository;

import com.itshaharcha.user.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    // Child collections are loaded lazily inside transactional service methods to
    // avoid MultipleBagFetchException from fetching several List collections at once.
    Optional<Profile> findByAccountId(UUID accountId);

    boolean existsByAccountId(UUID accountId);
}
