package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.dto.response.ExamResultResponse;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.entity.ExamType;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface ScoringService {

    ExamResultResponse getResult(UUID sessionId);

    PageResponse<ExamResultResponse> listMyResults(ExamType examType, Instant from, Instant to, Pageable pageable);
}
