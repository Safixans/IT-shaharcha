package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.dto.response.ExamDetailResponse;
import com.itshaharcha.assessment.dto.response.ExamResponse;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.entity.ExamType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CatalogService {

    PageResponse<ExamResponse> listExams(ExamType examType, Pageable pageable);

    ExamDetailResponse getExam(UUID examId);
}
