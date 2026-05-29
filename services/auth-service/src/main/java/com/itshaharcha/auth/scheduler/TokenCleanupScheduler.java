package com.itshaharcha.auth.scheduler;

import com.itshaharcha.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Periodically purges expired refresh tokens. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "${app.cleanup.refresh-tokens-cron:0 0 3 * * *}")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        int removed = refreshTokenRepository.deleteAllExpiredBefore(Instant.now());
        if (removed > 0) {
            log.info("Purged {} expired refresh tokens", removed);
        }
    }
}
