package com.itshaharcha.portfolio.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.portfolio.dto.request.PublishInput;
import com.itshaharcha.portfolio.dto.response.PortfolioResponse;
import com.itshaharcha.portfolio.entity.PortfolioProfile;
import com.itshaharcha.portfolio.entity.Visibility;
import com.itshaharcha.portfolio.event.PortfolioEventPublisher;
import com.itshaharcha.portfolio.mapper.PortfolioMapper;
import com.itshaharcha.portfolio.repository.CertificateRepository;
import com.itshaharcha.portfolio.repository.EducationRepository;
import com.itshaharcha.portfolio.repository.PortfolioItemRepository;
import com.itshaharcha.portfolio.repository.PortfolioProfileRepository;
import com.itshaharcha.portfolio.service.impl.PortfolioServiceImpl;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplTest {

    @Mock private PortfolioProfileRepository profileRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private EducationRepository educationRepository;
    @Mock private PortfolioItemRepository itemRepository;
    @Mock private PortfolioMapper mapper;
    @Mock private PortfolioEventPublisher events;
    @InjectMocks private PortfolioServiceImpl service;

    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(accountId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));
        lenient().when(certificateRepository.findByAccountIdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of());
        lenient().when(educationRepository.findByAccountIdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of());
        lenient().when(itemRepository.findByAccountIdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of());
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publish_firstTime_generatesHandleDefaultsPublicAndEmits() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(PortfolioProfile.class))).thenAnswer(i -> {
            PortfolioProfile p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PortfolioResponse response = service.publish(new PublishInput(null, null));

        assertThat(response.visibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(response.handle()).isNotBlank();
        assertThat(response.publishedAt()).isNotNull();
        verify(events).emit(eq("portfolio.published"), eq("portfolio"), any(UUID.class), any());
    }

    @Test
    void publish_handleTakenByAnotherAccount_conflict() {
        PortfolioProfile other = new PortfolioProfile();
        other.setAccountId(UUID.randomUUID());
        other.setHandle("taken");
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(profileRepository.findByHandle("taken")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.publish(new PublishInput("taken", Visibility.PUBLIC)))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    void getPublic_privateProfile_notFound() {
        PortfolioProfile profile = new PortfolioProfile();
        profile.setAccountId(accountId);
        profile.setHandle("me");
        profile.setVisibility(Visibility.PRIVATE);
        profile.setPublishedAt(Instant.now());
        when(profileRepository.findByHandle("me")).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> service.getPublic("me"))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    void getPublic_publishedPublic_returnsAssembled() {
        PortfolioProfile profile = new PortfolioProfile();
        profile.setAccountId(accountId);
        profile.setHandle("me");
        profile.setVisibility(Visibility.PUBLIC);
        profile.setPublishedAt(Instant.now());
        when(profileRepository.findByHandle("me")).thenReturn(Optional.of(profile));

        PortfolioResponse response = service.getPublic("me");

        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.handle()).isEqualTo("me");
    }
}
