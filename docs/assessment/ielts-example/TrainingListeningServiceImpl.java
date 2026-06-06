package uz.thompson.appmockielts.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.thompson.appmockielts.entity.*;
import uz.thompson.appmockielts.enums.RoleEnum;
import uz.thompson.appmockielts.enums.TrainingRecordStatus;
import uz.thompson.appmockielts.enums.TrainingType;
import uz.thompson.appmockielts.enums.UnitOrder;
import uz.thompson.appmockielts.exception.RestException;
import uz.thompson.appmockielts.mapper.TrainingListeningRecordMapper;
import uz.thompson.appmockielts.mapper.TrainingMapper;
import uz.thompson.appmockielts.payload.PaginationDto;
import uz.thompson.appmockielts.payload.ResponseDto;
import uz.thompson.appmockielts.payload.mock.AnswerDto;
import uz.thompson.appmockielts.payload.training.*;
import uz.thompson.appmockielts.repository.TrainingOptionRepository;
import uz.thompson.appmockielts.repository.TrainingProblemRepository;
import uz.thompson.appmockielts.repository.TrainingRecordRepository;
import uz.thompson.appmockielts.repository.TrainingRepository;
import uz.thompson.appmockielts.service.AttachmentService;
import uz.thompson.appmockielts.service.TrainingActivityService;
import uz.thompson.appmockielts.service.TrainingListeningService;
import uz.thompson.appmockielts.service.TrainingProblemService;
import uz.thompson.appmockielts.utils.CommonUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * Created by dilshodlatipov748@gmail.com on 21/02/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingListeningServiceImpl implements TrainingListeningService {
    private final TrainingMapper trainingMapper;
    private final AttachmentService attachmentService;
    private final TrainingRepository trainingRepository;
    private final TrainingProblemService trainingProblemService;
    private final TrainingRecordRepository trainingRecordRepository;
    private final TrainingListeningRecordMapper trainingRecordMapper;
    private final TrainingProblemRepository trainingProblemRepository;
    private final TrainingOptionRepository trainingOptionRepository;
    private final TrainingActivityService trainingActivityService;


    @Override
    public ResponseDto<TrainingListeningDto> get(UUID listeningId) {
        log.info("TrainingListeningServiceImpl.get: listeningId = {}", listeningId);
        Training training = findById(listeningId);
        return ResponseDto.successResponse(trainingMapper.mapToListeningDto(training));
    }

    @Override
    public ResponseDto<PaginationDto<List<TrainingListeningMetaDto>>> get(User currentUser, Integer unit, Integer page, Integer size) {
        log.info("TrainingListeningServiceImpl.get: currentUser = {}, unit = {}, page = {}, size = {}", currentUser.getId(), unit, page, size);
        CommonUtils.checkPageAndSize(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Training> trainings;
        if (Objects.equals(currentUser.getRole(), RoleEnum.STUDENT)) {
            trainings = trainingRepository.findAllByTypeAndDeletedFalseAndActiveTrueOrderByCreatedAtDesc(TrainingType.LISTENING, pageable);
        } else {
            trainings = trainingRepository.findAllByTypeAndUnitAndDeletedFalseOrderByCreatedAtDesc(TrainingType.LISTENING,
                    switch (unit) {
                        case 1 -> UnitOrder.ONE;
                        case 2 -> UnitOrder.TWO;
                        case 3 -> UnitOrder.THREE;
                        case 4 -> UnitOrder.FOUR;
                        default -> throw RestException.restThrow("Unexpected value: " + unit);
                    }, pageable);
        }
        return ResponseDto.successResponse(PaginationDto.makeForPage(trainings.getTotalPages(),
                page, size,
                trainings.getTotalElements(),
                trainingMapper.mapToListeningDto(trainings.getContent())));
    }

    @Override
    @Transactional
    public ResponseDto<TrainingListeningDto> add(TrainingListeningAddDto request) {
        log.info("TrainingListeningServiceImpl.add: request = {}", request);

        Attachment audio = attachmentService.findAttachmentById(request.getAudioId());
        if (audio.getContentType() == null || !audio.getContentType().startsWith("audio")) {
            throw RestException.restThrow("Audio type file is required");
        }

        audio.setLength(attachmentService.getAudioLength(audio));
        attachmentService.save(audio);

        Training training = Training.builder()
                .title(request.getTitle())
                .document(audio)
                .active(true)
                .unit(request.getUnit())
                .originalSectionData(request.getQuestions())
                .sectionData("")
                .type(TrainingType.LISTENING)
                .maxCorrectAnswers(-1)
                .minTimeSpent(-1)
                .build();
        trainingRepository.save(training);

        String updatedHtml = trainingProblemService.addProblems(request.getQuestions(), training, 10);

        training.setSectionData(updatedHtml);
        trainingRepository.save(training);

        return ResponseDto.successResponse(
                trainingMapper.mapToListeningDto(training)
        );
    }

    @Override
    @Transactional
    public ResponseDto<TrainingListeningDto> update(UUID listeningId, TrainingListeningUpdateDto request) {
        log.info("TrainingListeningServiceImpl.update: listeningId = {}", listeningId);

        Training training = findById(listeningId);

        Attachment audio = attachmentService.findAttachmentById(request.getAudioId());
        if (audio.getContentType() == null || !audio.getContentType().startsWith("audio")) {
            throw RestException.restThrow("Audio type file is required");
        }

        audio.setLength(attachmentService.getAudioLength(audio));
        attachmentService.save(audio);

        training.setTitle(request.getTitle());
        training.setDocument(audio);

        String updateHtml = trainingProblemService.updateProblems(request.getQuestions(), training, 10);

        if (request.getQuestions() != null && !request.getQuestions().trim().isEmpty()
                && !Objects.equals(request.getQuestions().trim(), training.getOriginalSectionData())) {
            training.setOriginalSectionData(request.getQuestions().trim());
        }

        training.setSectionData(updateHtml);
        trainingRepository.save(training);

        return ResponseDto.successResponse(
                trainingMapper.mapToListeningDto(training)
        );
    }

    @Override
    public ResponseDto<Boolean> changeStatus(UUID listeningId) {
        log.info("TrainingListeningServiceImpl.changeStatus: listeningId = {}", listeningId);
        Training training = findById(listeningId);
        training.setActive(!training.isActive());
        trainingRepository.save(training);
        return ResponseDto.successResponse(true);
    }

    @Override
    public ResponseDto<Boolean> delete(UUID listeningId) {
        log.info("TrainingListeningServiceImpl.delete: listeningId = {}", listeningId);
        trainingRepository.markAsDeleted(listeningId);
        return ResponseDto.successResponse(Boolean.TRUE);
    }

    @Override
    @Transactional
    public ResponseDto<TrainingListeningExamDto> start(User currentUser, UUID listeningId) {
        log.info("TrainingListeningServiceImpl.start: currentUser = {}, listeningId = {}", currentUser.getId(), listeningId);
        Optional<TrainingRecord> lastTraining = trainingRecordRepository.findFirstByStudent_IdAndTypeOrderByCreatedAtDesc(currentUser.getId(), TrainingType.LISTENING);

        if (lastTraining.isPresent()) {
            TrainingRecord trainingRecord = lastTraining.get();
            if (Objects.equals(trainingRecord.getStatus(), TrainingRecordStatus.IN_PROCESS)) {
                if (CommonUtils.dateTime().isBefore(trainingRecord.getDeadline())
                        && Objects.equals(listeningId, trainingRecord.getTraining().getId())) {
                    return ResponseDto.successResponse(trainingRecordMapper.mapToListeningExamDto(trainingRecord),
                            "You have an active training session.");
                } else {
                    submitPrivately(currentUser, trainingRecord.getId(), TrainingListeningSubmitDto.builder()
                            .answers(Collections.emptyList())
                            .build(), trainingRecord.getStartTime().plusSeconds(10));
                }
            }
        }

        Training training = findById(listeningId);

        var now = CommonUtils.dateTime();
        TrainingRecord trainingRecord = TrainingRecord.builder()
                .student(currentUser)
                .training(training)
                .type(TrainingType.LISTENING)
                .title(training.getTitle())
                .status(TrainingRecordStatus.IN_PROCESS)
                .startTime(now)
                .deadline(now.plusMinutes(training.getDocument().getLength() + 2))
                .build();

        trainingRecordRepository.save(trainingRecord);
        return ResponseDto.successResponse(trainingRecordMapper.mapToListeningExamDto(trainingRecord));
    }

    @Override
    @Transactional
    public ResponseDto<TrainingReportDto> submit(User currentUser, UUID reportId, TrainingListeningSubmitDto request) {
        return submitPrivately(currentUser, reportId, request, CommonUtils.dateTime());
    }

    @Override
    public ResponseDto<TrainingReportDto> getReport(User currentUser, UUID reportId) {
        log.info("TrainingListeningServiceImpl.getReport: currentUser = {}, reportId = {}", currentUser.getId(), reportId);
        TrainingRecord record = findRecordById(reportId);
        if (!Objects.equals(currentUser.getId(), record.getStudent().getId())) {
            throw RestException.restThrow("Report not found", HttpStatus.NOT_FOUND);
        }
        return ResponseDto.successResponse(trainingRecordMapper.mapToListeningReportDto(record));
    }

    @Override
    public ResponseDto<PaginationDto<List<TrainingReportDto>>> getReports(User currentUser, Integer unit, Integer page, Integer size) {
        log.info("TrainingListeningServiceImpl.getReports: currentUser = {}, unit = {}, page = {}, size = {}", currentUser.getId(), unit, page, size);

        CommonUtils.checkPageAndSize(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingRecord> trainingRecords;
        if (unit != null) {
            trainingRecords = trainingRecordRepository.findByStudent_IdAndTypeAndTraining_UnitOrderByCreatedAtDesc(currentUser.getId(),
                    TrainingType.LISTENING,
                    switch (unit) {
                        case 1 -> UnitOrder.ONE;
                        case 2 -> UnitOrder.TWO;
                        case 3 -> UnitOrder.THREE;
                        case 4 -> UnitOrder.FOUR;
                        default -> throw RestException.restThrow("Unexpected value: " + unit);
                    },
                    pageable);
        } else {
            trainingRecords = trainingRecordRepository.findByStudent_IdAndTypeOrderByCreatedAtDesc(currentUser.getId(), TrainingType.LISTENING, pageable);
        }

        return ResponseDto.successResponse(PaginationDto.makeForPage(
                trainingRecords.getTotalPages(),
                page,
                size,
                trainingRecords.getTotalElements(),
                trainingRecordMapper.mapToListeningReportDto(trainingRecords.getContent())
        ));
    }

    private Training findById(UUID id) {
        return trainingRepository.findByIdAndTypeAndDeletedFalse(id, TrainingType.LISTENING)
                .orElseThrow(() -> RestException.restThrow("Listening not found", HttpStatus.NOT_FOUND));
    }

    private TrainingRecord findRecordById(UUID id) {
        return trainingRecordRepository.findById(id)
                .orElseThrow(() -> RestException.restThrow("Report not found", HttpStatus.NOT_FOUND));
    }

/*    private List<TrainingAnswer> getAnswers(User user, List<AnswerDto> answers, UUID trainingId) {
        log.info("TrainingListeningServiceImpl.getAnswers: user = {}, answers = {}, trainingId = {}",
                user.getId(), answers, trainingId);

        if (answers == null) {
            answers = Collections.emptyList();
        }

        List<TrainingProblem> problems = trainingProblemRepository.findAllByTraining_Id(trainingId);

        Map<String, List<String>> correctOptions = trainingOptionRepository.findByProblemInAndCorrectTrue(problems).stream()
                .collect(Collectors.groupingBy(
                        TrainingOption::getOrderIndex,
                        Collectors.mapping(TrainingOption::getOption, Collectors.toList())
                ));

        Map<String, String> userOptions = answers.stream()
                .map(AnswerDto::getAnswers)
                .filter(Objects::nonNull)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first
                ));

        return correctOptions.keySet().stream()
                .map(orderIndex -> {
                    String userOption = userOptions.getOrDefault(orderIndex, "").trim();
                    List<String> options = correctOptions.get(orderIndex);

                    if (userOption.isEmpty()) {
                        for (String userChoiceOrderIndex : userOptions.keySet()) {
                            if (orderIndex.contains("_") && userChoiceOrderIndex.contains("_")) {
                                String correctOptionOrderIndex = getProblemOrderIndex(orderIndex);
                                if (userChoiceOrderIndex.startsWith(correctOptionOrderIndex) && !correctOptions.containsKey(userChoiceOrderIndex)) {
                                    userOption = ofNullable(userOptions.get(userChoiceOrderIndex)).orElse("").trim();
                                    userOptions.remove(userChoiceOrderIndex);
                                    break;
                                }
                            } else if (orderIndex.startsWith(getProblemOrderIndex(userChoiceOrderIndex))) {
                                userOption = ofNullable(userOptions.get(userChoiceOrderIndex)).orElse("").trim();
                                userOptions.remove(userChoiceOrderIndex);
                                break;
                            }
                        }
                    }

                    return TrainingAnswer.builder()
                            .orderIndex(orderIndex)
                            .correctOptions(options)
                            .answer(userOption)
                            .correct(checkAnswer(correctOptions.get(orderIndex), userOption))
                            .build();
                }).toList();
    }*/

    private List<TrainingAnswer> getAnswers(User user, List<AnswerDto> answers, UUID trainingId) {
        log.info("TrainingListeningServiceImpl.getAnswers: user = {}, answers = {}, trainingId = {}",
                user.getId(), answers, trainingId);

        if (answers == null) {
            answers = Collections.emptyList();
        }

        List<TrainingProblem> problems = trainingProblemRepository.findAllByTraining_Id(trainingId);

        Map<UUID, Map<String, String>> mappedAnswers = answers.stream()
                .collect(Collectors.toMap(
                        AnswerDto::getProblemId,
                        AnswerDto::getAnswers
                ));

        return problems.stream()
                .flatMap(problem -> {
                    // Always default to empty map — never depend on user having submitted anything.
                    Map<String, String> userAnswers =
                            mappedAnswers.getOrDefault(problem.getId(), Map.of());

                    return switch (problem.getProblemType()) {
                        case INPUT, RADIO, SELECT -> gradeSingle(user, problem, userAnswers);
                        case MULTIPLE_CHOICE -> gradeMultiSelect(user, problem, userAnswers);
                    };
                })
                .toList();
    }

    Stream<TrainingAnswer> gradeSingle(User student, TrainingProblem problem, Map<String, String> userAnswers) {
        log.info("Grade single answer: user.id = {}, userAnswers = {}", student.getId(), userAnswers);
        List<String> correctOptions = getCorrectOptions(problem);

        String submitted = userAnswers.values().stream()
                .findFirst()
                .orElse("")
                .trim();

        return Stream.of(TrainingAnswer.builder()
                .orderIndex(problem.getOrderIndex())
                .correctOptions(correctOptions)
                .answer(submitted)
                .correct(checkAnswer(correctOptions, submitted))
                .build());
    }

    Stream<TrainingAnswer> gradeMultiSelect(User student, TrainingProblem problem, Map<String, String> userAnswers) {
        log.info("Gradle multiple select answer: user.id = {}, userAnswers = {}", student.getId(), userAnswers);
        // Group options by slot, preserving insertion order.
        List<TrainingOption> bySlot = problem.getOptions()
                .stream()
                .filter(op -> Boolean.TRUE.equals(op.getCorrect()))
                .toList();

        List<String> correctOptions = getCorrectOptions(problem);

        // Flatten all submitted values into a mutable pool — keys are irrelevant.
        List<String> pool = userAnswers.values().stream()
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toCollection(() -> new ArrayList<>(2)));

        return bySlot.stream()
                .map(option -> {
                    if (pool.contains(option.getOption())) {
                        pool.remove(option.getOption());
                        return TrainingAnswer.builder()
                                .orderIndex(option.getOrderIndex())
                                .correctOptions(List.of(option.getOption()))
                                .answer(option.getOption())
                                .correct(true)
                                .build();
                    }

                    String first = pool.stream()
                            .filter(s -> !correctOptions.contains(s))
                            .findFirst()
                            .orElse("");

                    pool.remove(first);
                    return TrainingAnswer.builder()
                            .orderIndex(option.getOrderIndex())
                            .correctOptions(List.of(option.getOption()))
                            .answer(first)
                            .correct(false)
                            .build();
                });
    }

    static List<String> getCorrectOptions(TrainingProblem problem) {
        return problem.getOptions()
                .stream()
                .filter(op -> Boolean.TRUE.equals(op.getCorrect()))
                .map(TrainingOption::getOption)
                .toList();
    }

    private boolean checkAnswer(List<String> correctOptions, String answer) {
        return correctOptions.contains(answer);
    }

    @NotNull
    private ResponseDto<TrainingReportDto> submitPrivately(User currentUser, UUID reportId, TrainingListeningSubmitDto request, LocalDateTime endTime) {
        log.info("TrainingListeningServiceImpl.submit: currentUser = {}, reportId = {}, request = {}", currentUser.getId(), reportId, request);
        TrainingRecord record = findRecordById(reportId);
        if (!Objects.equals(currentUser.getId(), record.getStudent().getId())) {
            throw RestException.restThrow("Report not found", HttpStatus.NOT_FOUND);
        }

        if (!Objects.equals(record.getStatus(), TrainingRecordStatus.IN_PROCESS)) {
            throw RestException.restThrow("This training session is not in process");
        }
        LocalDateTime now = CommonUtils.dateTime();
        if (now.isAfter(record.getDeadline())) {
            request.setAnswers(Collections.emptyList());
        }
        record.setStatus(TrainingRecordStatus.COMPLETED);
        record.setEndTime(endTime);
        List<TrainingAnswer> answers = getAnswers(currentUser, request.getAnswers(), record.getTraining().getId());
        record.setAnswers(answers);
        int correctCount = (int) answers.stream()
                .filter(TrainingAnswer::isCorrect)
                .count();
        int incorrectCount = (int) answers.stream()
                .filter(trainingAnswer -> !trainingAnswer.isCorrect())
                .count();
        record.setCorrectAnswers(correctCount);
        record.setIncorrectAnswers(incorrectCount);
        record = trainingActivityService.saveTrainingRecord(record);

        Training training = record.getTraining();

        long timeSpent = now.toEpochSecond(ZoneOffset.UTC)
                - record.getStartTime().toEpochSecond(ZoneOffset.UTC);

        int maxCorrect = training.getMaxCorrectAnswers();

        if (correctCount >= maxCorrect) {
            if (correctCount > maxCorrect) {
                training.setMaxCorrectAnswers(correctCount);
                training.setMinTimeSpent(timeSpent);
            } else {
                training.setMinTimeSpent(Math.min(training.getMinTimeSpent(), timeSpent));
            }
        }
        trainingRepository.save(training);

        return ResponseDto.successResponse(trainingRecordMapper.mapToListeningReportDto(record));
    }
}
