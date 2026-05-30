package com.itshaharcha.portfolio.repository;

import com.itshaharcha.portfolio.entity.PortfolioProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioProfileRepository extends JpaRepository<PortfolioProfile, UUID> {

    Optional<PortfolioProfile> findByAccountId(UUID accountId);

    Optional<PortfolioProfile> findByHandle(String handle);
}
