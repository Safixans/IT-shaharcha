package com.itshaharcha.portfolio.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.portfolio.dto.request.CertificateCreate;
import com.itshaharcha.portfolio.dto.request.VerifyInput;
import com.itshaharcha.portfolio.entity.Certificate;
import com.itshaharcha.portfolio.entity.VerificationStatus;
import com.itshaharcha.portfolio.event.PortfolioEventPublisher;
import com.itshaharcha.portfolio.mapper.PortfolioMapper;
import com.itshaharcha.portfolio.repository.CertificateRepository;
import com.itshaharcha.portfolio.repository.FileRepository;
import com.itshaharcha.portfolio.service.impl.CertificateServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    @Mock private CertificateRepository certificateRepository;
    @Mock private FileRepository fileRepository;
    @Mock private PortfolioMapper mapper;
    @Mock private PortfolioEventPublisher events;
    @InjectMocks private CertificateServiceImpl service;

    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(accountId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_persistsPendingAndEmits() {
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(i -> {
            Certificate c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        service.create(new CertificateCreate("AWS CCP", "Amazon", null, null, null));

        verify(certificateRepository).save(any(Certificate.class));
        verify(events).emit(eq("portfolio.certificate.uploaded"), eq("certificate"), any(UUID.class), any());
    }

    @Test
    void create_withUnknownFileId_rejected() {
        UUID fileId = UUID.randomUUID();
        when(fileRepository.existsById(fileId)).thenReturn(false);

        assertThatThrownBy(() ->
                service.create(new CertificateCreate("X", null, null, fileId, null)))
                .isInstanceOf(ApplicationException.class);

        verify(certificateRepository, never()).save(any());
    }

    @Test
    void verify_approves_setsVerifiedAndEmits() {
        UUID certId = UUID.randomUUID();
        Certificate cert = new Certificate();
        cert.setId(certId);
        cert.setAccountId(UUID.randomUUID()); // owned by someone else; reviewer verifies it
        cert.setStatus(VerificationStatus.PENDING);
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(cert));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(i -> i.getArgument(0));

        service.verify(certId, new VerifyInput(true, "looks good"));

        assertThat(cert.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(cert.getVerifiedAt()).isNotNull();
        verify(events).emit(eq("portfolio.certificate.verified"), eq("certificate"), eq(certId), any());
    }

    @Test
    void verify_rejection_setsRejected() {
        UUID certId = UUID.randomUUID();
        Certificate cert = new Certificate();
        cert.setId(certId);
        cert.setStatus(VerificationStatus.PENDING);
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(cert));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(i -> i.getArgument(0));

        service.verify(certId, new VerifyInput(false, "blurry scan"));

        assertThat(cert.getStatus()).isEqualTo(VerificationStatus.REJECTED);
    }

    @Test
    void get_notOwned_throwsNotFound() {
        UUID certId = UUID.randomUUID();
        when(certificateRepository.findByIdAndAccountId(certId, accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(certId))
                .isInstanceOf(ApplicationException.class);
    }
}
