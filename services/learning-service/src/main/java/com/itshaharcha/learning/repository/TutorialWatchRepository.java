package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.TutorialWatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TutorialWatchRepository extends JpaRepository<TutorialWatch, UUID> {

    Optional<TutorialWatch> findByTutorialIdAndAccountId(UUID tutorialId, UUID accountId);

    long countByAccountIdAndCompletedTrue(UUID accountId);
}
