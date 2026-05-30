package com.itshaharcha.analytics.repository;

import com.itshaharcha.analytics.entity.DomainProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DomainProgressRepository extends JpaRepository<DomainProgress, UUID> {

    List<DomainProgress> findByAccountId(UUID accountId);

    Optional<DomainProgress> findByAccountIdAndDomain(UUID accountId, String domain);

    // ---- per-domain leaderboard ----
    Page<DomainProgress> findByDomainOrderByPointsDescAccountIdAsc(String domain, Pageable pageable);

    /** Accounts in this domain with strictly more points than the given threshold (for rank). */
    long countByDomainAndPointsGreaterThan(String domain, long points);

    // ---- overall (cross-domain) leaderboard: sum points per account ----
    @Query("""
            SELECT p.accountId AS accountId, SUM(p.points) AS points
            FROM DomainProgress p
            GROUP BY p.accountId
            ORDER BY SUM(p.points) DESC, p.accountId ASC
            """)
    List<AccountPoints> overallBoard(Pageable pageable);

    /** Count of accounts whose summed cross-domain points strictly exceed :points (for overall rank). */
    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT account_id FROM domain_progress
                GROUP BY account_id
                HAVING SUM(points) > :points
            ) ranked
            """, nativeQuery = true)
    long countAccountsWithOverallPointsGreaterThan(@Param("points") long points);
}
