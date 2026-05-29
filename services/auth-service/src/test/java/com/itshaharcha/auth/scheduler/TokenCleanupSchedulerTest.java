package com.itshaharcha.auth.scheduler;

import com.itshaharcha.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks private TokenCleanupScheduler scheduler;

    @Test
    void purge_deletesExpiredTokens_andLogsWhenRemoved() {
        when(refreshTokenRepository.deleteAllExpiredBefore(any(Instant.class))).thenReturn(3);
        scheduler.purgeExpiredRefreshTokens();
        verify(refreshTokenRepository).deleteAllExpiredBefore(any(Instant.class));
    }

    @Test
    void purge_isSilent_whenNothingRemoved() {
        when(refreshTokenRepository.deleteAllExpiredBefore(any(Instant.class))).thenReturn(0);
        scheduler.purgeExpiredRefreshTokens();
        verify(refreshTokenRepository).deleteAllExpiredBefore(any(Instant.class));
    }
}
