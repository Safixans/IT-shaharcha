package com.itshaharcha.assessment.service.impl;

import com.itshaharcha.assessment.dto.response.ExamResultResponse;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.entity.ExamResult;
import com.itshaharcha.assessment.entity.ExamType;
import com.itshaharcha.assessment.mapper.AssessmentMapper;
import com.itshaharcha.assessment.repository.ExamResultRepository;
import com.itshaharcha.assessment.security.SecurityUtils;
import com.itshaharcha.assessment.service.ScoringService;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScoringServiceImpl implements ScoringService {

    private final ExamResultRepository resultRepository;
    private final AssessmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ExamResultResponse getResult(UUID sessionId) {
        ExamResult result = resultRepository.findBySessionId(sessionId)
                .orElseThrow(() -> ApplicationException.notFound("Result not found"));
        if (!result.getAccountId().equals(SecurityUtils.currentAccountId()) && !SecurityUtils.isAdmin()) {
            throw ApplicationException.forbidden("Cannot read another candidate's result");
        }
        return mapper.toResultResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExamResultResponse> listMyResults(ExamType examType, Instant from, Instant to,
                                                          Pageable pageable) {
        UUID accountId = SecurityUtils.currentAccountId();
        return PageResponse.from(
                resultRepository.search(accountId, examType, from, to, pageable),
                mapper::toResultResponse);
    }
}
