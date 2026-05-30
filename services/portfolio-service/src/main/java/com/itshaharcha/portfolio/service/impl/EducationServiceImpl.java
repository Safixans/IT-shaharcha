package com.itshaharcha.portfolio.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.portfolio.dto.request.EducationCreate;
import com.itshaharcha.portfolio.dto.response.EducationResponse;
import com.itshaharcha.portfolio.entity.Education;
import com.itshaharcha.portfolio.event.PortfolioEventPublisher;
import com.itshaharcha.portfolio.mapper.PortfolioMapper;
import com.itshaharcha.portfolio.repository.EducationRepository;
import com.itshaharcha.portfolio.security.SecurityUtils;
import com.itshaharcha.portfolio.service.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final PortfolioMapper mapper;
    private final PortfolioEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> list() {
        UUID accountId = SecurityUtils.currentAccountId();
        return educationRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(mapper::toEducationResponse)
                .toList();
    }

    @Override
    @Transactional
    public EducationResponse add(EducationCreate input) {
        UUID accountId = SecurityUtils.currentAccountId();
        Education education = new Education();
        education.setAccountId(accountId);
        education.setInstitution(input.institution());
        education.setDegree(input.degree());
        education.setFieldOfStudy(input.fieldOfStudy());
        education.setStartDate(input.startDate());
        education.setEndDate(input.endDate());
        education.setDescription(input.description());
        Education saved = educationRepository.save(education);

        Map<String, Object> data = new HashMap<>();
        data.put("institution", saved.getInstitution());
        events.emit("portfolio.education.added", "education", saved.getId(), data);
        return mapper.toEducationResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID educationId) {
        UUID accountId = SecurityUtils.currentAccountId();
        Education education = educationRepository.findByIdAndAccountId(educationId, accountId)
                .orElseThrow(() -> ApplicationException.notFound("Education entry not found"));
        educationRepository.delete(education);
    }
}
