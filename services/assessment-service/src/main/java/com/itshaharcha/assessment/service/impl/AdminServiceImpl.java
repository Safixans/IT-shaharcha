package com.itshaharcha.assessment.service.impl;

import com.itshaharcha.assessment.dto.request.ExamInput;
import com.itshaharcha.assessment.dto.request.QuestionInput;
import com.itshaharcha.assessment.dto.request.SectionInput;
import com.itshaharcha.assessment.dto.response.ExamResponse;
import com.itshaharcha.assessment.dto.response.QuestionResponse;
import com.itshaharcha.assessment.dto.response.SectionResponse;
import com.itshaharcha.assessment.entity.Exam;
import com.itshaharcha.assessment.entity.Question;
import com.itshaharcha.assessment.entity.Section;
import com.itshaharcha.assessment.mapper.AssessmentMapper;
import com.itshaharcha.assessment.repository.ExamRepository;
import com.itshaharcha.assessment.repository.QuestionRepository;
import com.itshaharcha.assessment.repository.SectionRepository;
import com.itshaharcha.assessment.service.AdminService;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ExamRepository examRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentMapper mapper;

    @Override
    @Transactional
    public ExamResponse createExam(ExamInput input) {
        Exam exam = new Exam();
        applyExam(exam, input);
        return mapper.toExamResponse(examRepository.save(exam), 0);
    }

    @Override
    @Transactional
    public ExamResponse updateExam(UUID examId, ExamInput input) {
        Exam exam = requireExam(examId);
        applyExam(exam, input);
        Exam saved = examRepository.save(exam);
        return mapper.toExamResponse(saved, (int) sectionRepository.countByExamId(examId));
    }

    @Override
    @Transactional
    public void deleteExam(UUID examId) {
        examRepository.delete(requireExam(examId));
    }

    @Override
    @Transactional
    public SectionResponse createSection(UUID examId, SectionInput input) {
        requireExam(examId);
        Section section = new Section();
        section.setExamId(examId);
        applySection(section, input);
        return mapper.toSectionResponse(sectionRepository.save(section), 0);
    }

    @Override
    @Transactional
    public SectionResponse updateSection(UUID sectionId, SectionInput input) {
        Section section = requireSection(sectionId);
        applySection(section, input);
        Section saved = sectionRepository.save(section);
        return mapper.toSectionResponse(saved, (int) questionRepository.countBySectionId(sectionId));
    }

    @Override
    @Transactional
    public void deleteSection(UUID sectionId) {
        sectionRepository.delete(requireSection(sectionId));
    }

    @Override
    @Transactional
    public QuestionResponse createQuestion(UUID sectionId, QuestionInput input) {
        Section section = requireSection(sectionId);
        Question question = new Question();
        question.setSectionId(sectionId);
        question.setExamId(section.getExamId());
        applyQuestion(question, input);
        return mapper.toQuestionResponse(questionRepository.save(question));
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(UUID questionId, QuestionInput input) {
        Question question = requireQuestion(questionId);
        applyQuestion(question, input);
        return mapper.toQuestionResponse(questionRepository.save(question));
    }

    @Override
    @Transactional
    public void deleteQuestion(UUID questionId) {
        questionRepository.delete(requireQuestion(questionId));
    }

    private void applyExam(Exam exam, ExamInput input) {
        exam.setTitle(input.title());
        exam.setExamType(input.examType());
        exam.setDescription(input.description());
        exam.setDurationMinutes(input.durationMinutes());
        if (input.isRealExam() != null) {
            exam.setRealExam(input.isRealExam());
        }
    }

    private void applySection(Section section, SectionInput input) {
        section.setName(input.name());
        if (input.order() != null) {
            section.setOrderIndex(input.order());
        }
        section.setDurationMinutes(input.durationMinutes());
    }

    private void applyQuestion(Question question, QuestionInput input) {
        question.setPrompt(input.prompt());
        question.setKind(input.kind());
        if (input.order() != null) {
            question.setOrderIndex(input.order());
        }
        if (input.points() != null) {
            question.setPoints(input.points());
        }
        question.setChoices(input.choices());
        question.setCorrectAnswer(input.correctAnswer());
        question.setExplanation(input.explanation());
    }

    private Exam requireExam(UUID id) {
        return examRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Exam not found"));
    }

    private Section requireSection(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Section not found"));
    }

    private Question requireQuestion(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Question not found"));
    }
}
