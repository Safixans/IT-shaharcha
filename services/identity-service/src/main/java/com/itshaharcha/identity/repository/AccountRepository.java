package com.itshaharcha.identity.repository;

import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByEmailIgnoreCase(String email);

    Optional<Account> findByUsernameIgnoreCase(String username);

    Optional<Account> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("""
            select a from Account a
            where (:status is null or a.status = :status)
              and (cast(:q as string) is null
                   or lower(a.username) like lower(concat('%', cast(:q as string), '%'))
                   or lower(a.email) like lower(concat('%', cast(:q as string), '%')))
            """)
    Page<Account> search(@Param("status") AccountStatus status,
                         @Param("q") String q,
                         Pageable pageable);
}
