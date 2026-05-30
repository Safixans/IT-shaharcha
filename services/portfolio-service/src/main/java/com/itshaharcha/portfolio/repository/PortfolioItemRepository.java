package com.itshaharcha.portfolio.repository;

import com.itshaharcha.portfolio.entity.ItemKind;
import com.itshaharcha.portfolio.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, UUID> {

    List<PortfolioItem> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<PortfolioItem> findByAccountIdAndKindOrderByCreatedAtDesc(UUID accountId, ItemKind kind);

    Optional<PortfolioItem> findByIdAndAccountId(UUID id, UUID accountId);

    long countByAccountId(UUID accountId);
}
