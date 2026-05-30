package com.itshaharcha.assessment.service.impl;

import com.itshaharcha.assessment.dto.response.ExamDetailResponse;
import com.itshaharcha.assessment.dto.response.ExamResponse;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.dto.response.SectionResponse;
import com.itshaharcha.assessment.entity.Exam;
import com.itshaharcha.assessment.entity.ExamType;
import com.itshaharcha.assessment.mapper.AssessmentMapper;
import com.itshaharcha.assessment.repository.ExamRepository;
import com.itshaharcha.assessment.repository.QuestionRepository;
import com.itshaharcha.assessment.repository.SectionRepository;
import com.itshaharcha.assessment.service.CatalogService;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final ExamRepository examRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExamResponse> listExams(ExamType examType, Pageable pageable) {
        return PageResponse.from(
                examRepository.search(examType, pageable),
                exam -> mapper.toExamResponse(exam, (int) sectionRepository.countByExamId(exam.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDetailResponse getExam(UUID examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> ApplicationException.notFound("Exam not found"));
        List<SectionResponse> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(examId).stream()
                .map(section -> mapper.toSectionResponse(
                        section, (int) questionRepository.countBySectionId(section.getId())))
                .toList();
        return mapper.toExamDetail(exam, sections.size(), sections);
    }
}
