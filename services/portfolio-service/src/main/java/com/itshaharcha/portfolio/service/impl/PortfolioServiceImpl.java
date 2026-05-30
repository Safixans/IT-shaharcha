package com.itshaharcha.portfolio.service.impl;

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
import com.itshaharcha.portfolio.security.SecurityUtils;
import com.itshaharcha.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioProfileRepository profileRepository;
    private final CertificateRepository certificateRepository;
    private final EducationRepository educationRepository;
    private final PortfolioItemRepository itemRepository;
    private final PortfolioMapper mapper;
    private final PortfolioEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getMine() {
        UUID accountId = SecurityUtils.currentAccountId();
        PortfolioProfile profile = profileRepository.findByAccountId(accountId).orElse(null);
        return assemble(accountId, profile);
    }

    @Override
    @Transactional
    public PortfolioResponse publish(PublishInput input) {
        UUID accountId = SecurityUtils.currentAccountId();
        PortfolioProfile profile = profileRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    PortfolioProfile p = new PortfolioProfile();
                    p.setAccountId(accountId);
                    return p;
                });

        String requestedHandle = input == null ? null : input.handle();
        if (requestedHandle != null && !requestedHandle.isBlank()) {
            profileRepository.findByHandle(requestedHandle).ifPresent(existing -> {
                if (!existing.getAccountId().equals(accountId)) {
                    throw ApplicationException.conflict("Handle is already taken");
                }
            });
            profile.setHandle(requestedHandle.trim());
        } else if (profile.getHandle() == null) {
            profile.setHandle(generateHandle(accountId));
        }

        Visibility visibility = (input == null || input.visibility() == null)
                ? Visibility.PUBLIC : input.visibility();
        profile.setVisibility(visibility);
        profile.setPublishedAt(Instant.now());
        PortfolioProfile saved = profileRepository.save(profile);

        Map<String, Object> data = new HashMap<>();
        data.put("handle", saved.getHandle());
        data.put("visibility", saved.getVisibility().wire());
        events.emit("portfolio.published", "portfolio", saved.getId(), data);
        return assemble(accountId, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPublic(String handle) {
        PortfolioProfile profile = profileRepository.findByHandle(handle)
                .filter(p -> p.getPublishedAt() != null)
                .filter(p -> p.getVisibility() != Visibility.PRIVATE)
                .orElseThrow(() -> ApplicationException.notFound("Published portfolio not found"));
        return assemble(profile.getAccountId(), profile);
    }

    /** Gather the account's certificates, education, and items into the portfolio view. */
    private PortfolioResponse assemble(UUID accountId, PortfolioProfile profile) {
        var certificates = certificateRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(mapper::toCertificateResponse).toList();
        var education = educationRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(mapper::toEducationResponse).toList();
        var items = itemRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(mapper::toItemResponse).toList();
        String handle = profile == null ? null : profile.getHandle();
        Visibility visibility = profile == null ? Visibility.PRIVATE : profile.getVisibility();
        Instant publishedAt = profile == null ? null : profile.getPublishedAt();
        return new PortfolioResponse(accountId, handle, visibility, publishedAt,
                certificates, education, items);
    }

    private String generateHandle(UUID accountId) {
        return "user-" + accountId.toString().substring(0, 8);
    }
}
