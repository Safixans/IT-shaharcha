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
import uz.thompson.appmockielts.mapper.TrainingMapper;
import uz.thompson.appmockielts.mapper.TrainingWritingRecordMapper;
import uz.thompson.appmockielts.payload.PaginationDto;
import uz.thompson.appmockielts.payload.ResponseDto;
import uz.thompson.appmockielts.payload.training.*;
import uz.thompson.appmockielts.repository.TrainingRecordRepository;
import uz.thompson.appmockielts.repository.TrainingRepository;
import uz.thompson.appmockielts.service.AttachmentService;
import uz.thompson.appmockielts.service.TrainingActivityService;
import uz.thompson.appmockielts.service.TrainingWritingService;
import uz.thompson.appmockielts.utils.CommonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by dilshodlatipov748@gmail.com on 19/02/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingWritingServiceImpl implements TrainingWritingService {
    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingWritingRecordMapper trainingRecordMapper;
    private final TrainingRecordRepository trainingRecordRepository;
    private final AttachmentService attachmentService;
    private final TrainingActivityService trainingActivityService;

    @Override
    public ResponseDto<TrainingWritingDto> get(UUID writingId) {
        log.info("TrainingWritingServiceImpl.get: writingId = {}", writingId);
        Training training = findById(writingId);
        return ResponseDto.successResponse(trainingMapper.mapToWritingDto(training));
    }

    @Override
    public ResponseDto<PaginationDto<List<TrainingWritingMetaDto>>> get(User currentUser, Integer unit, Integer page, Integer size) {
        log.info("TrainingWritingServiceImpl.get: currentUser = {}, unit = {}, page = {}, size = {}", currentUser.getId(), unit, page, size);
        CommonUtils.checkPageAndSize(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Training> trainings;
        if (Objects.equals(currentUser.getRole(), RoleEnum.STUDENT)) {
            trainings = trainingRepository.findAllByTypeAndDeletedFalseAndActiveTrueOrderByCreatedAtDesc(TrainingType.WRITING, pageable);
        } else {
            trainings = trainingRepository.findAllByTypeAndUnitAndDeletedFalseOrderByCreatedAtDesc(TrainingType.WRITING, unit == 1 ? UnitOrder.ONE : UnitOrder.TWO, pageable);
        }
        return ResponseDto.successResponse(PaginationDto.makeForPage(trainings.getTotalPages(),
                page,
                size,
                trainings.getTotalElements(),
                trainingMapper.mapToWritingMetaDto(trainings.getContent())));
    }

    @Override
    public ResponseDto<TrainingWritingDto> add(TrainingWritingAddDto request) {
        log.info("TrainingWritingServiceImpl.add: request = {}", request);

        if (!Objects.equals(request.getUnit(), UnitOrder.ONE) && !Objects.equals(request.getUnit(), UnitOrder.TWO)) {
            throw RestException.restThrow("Writing section order must be between 1 and 2");
        }

        Attachment image = null;
        if (request.getImageId() != null)
            image = attachmentService.findAttachmentById(request.getImageId());

        Training training = Training.builder()
                .title(request.getTitle())
                .active(true)
                .type(TrainingType.WRITING)
                .sectionData(request.getText())
                .originalSectionData(request.getText())
                .unit(request.getUnit())
                .document(image)
                .build();

        training = trainingRepository.save(training);

        return ResponseDto.successResponse(trainingMapper.mapToWritingDto(training));
    }

    @Override
    public ResponseDto<TrainingWritingDto> update(UUID writingId, TrainingWritingUpdateDto request) {
        log.info("TrainingWritingServiceImpl.update: writingId = {}, request = {}", writingId, request);

        Training training = findById(writingId);

        if (request.getImageId() != null) {
            Attachment image = attachmentService.findAttachmentById(request.getImageId());
            training.setDocument(image);
        }
        training.setTitle(request.getTitle());
        training.setSectionData(request.getText());
        training.setOriginalSectionData(request.getText());

        training = trainingRepository.save(training);

        return ResponseDto.successResponse(trainingMapper.mapToWritingDto(training));
    }

    @Override
    public ResponseDto<Boolean> changeStatus(UUID writingId) {
        Training training = findById(writingId);
        training.setActive(!training.isActive());
        trainingRepository.save(training);
        return ResponseDto.successResponse(true);
    }

    @Override
    public ResponseDto<Boolean> delete(UUID writingId) {
        log.info("TrainingWritingServiceImpl.delete: writingId = {}", writingId);
        trainingRepository.markAsDeleted(writingId);
        return ResponseDto.successResponse(Boolean.TRUE);
    }

    @Override
    @Transactional
    public ResponseDto<TrainingWritingExamDto> start(User currentUser, UUID writingId) {
        log.info("TrainingWritingServiceImpl.start: currentUser = {}, writingId = {}", currentUser.getId(), writingId);
        Optional<TrainingRecord> lastTraining = trainingRecordRepository.findFirstByStudent_IdAndTypeOrderByCreatedAtDesc(currentUser.getId(), TrainingType.WRITING);

        if (lastTraining.isPresent()) {
            TrainingRecord trainingRecord = lastTraining.get();
            if (Objects.equals(trainingRecord.getStatus(), TrainingRecordStatus.IN_PROCESS)) {
                if (Objects.equals(writingId, trainingRecord.getTraining().getId())) {
                    return ResponseDto.successResponse(trainingRecordMapper.mapToWritingExamDto(trainingRecord),
                            "You have an active training session.");
                } else {
                    submitPrivately(currentUser, trainingRecord.getId(), TrainingWritingSubmitDto.builder()
                            .essay("")
                            .build(), trainingRecord.getStartTime().plusSeconds(10));
                }
            }
        }

        Training training = findById(writingId);

        var now = CommonUtils.dateTime();
        TrainingRecord trainingRecord = TrainingRecord.builder()
                .student(currentUser)
                .training(training)
                .type(TrainingType.WRITING)
                .title(training.getTitle())
                .status(TrainingRecordStatus.IN_PROCESS)
                .startTime(now)
                .build();

        trainingRecordRepository.save(trainingRecord);
        return ResponseDto.successResponse(trainingRecordMapper.mapToWritingExamDto(trainingRecord));
    }

    @Override
    @Transactional
    public ResponseDto<TrainingReportDto> submit(User currentUser, UUID reportId, TrainingWritingSubmitDto request) {
        return submitPrivately(currentUser, reportId, request, CommonUtils.dateTime());
    }

    @Override
    public ResponseDto<TrainingReportDto> getReport(User currentUser, UUID reportId) {
        log.info("TrainingWritingServiceImpl.getReport: currentUser = {}, reportId = {}", currentUser.getId(), reportId);
        TrainingRecord record = findRecordById(reportId);
        if (!Objects.equals(currentUser.getId(), record.getStudent().getId())) {
            throw RestException.restThrow("Report not found", HttpStatus.NOT_FOUND);
        }
        return ResponseDto.successResponse(trainingRecordMapper.mapToWritingRecordDto(record));
    }

    @Override
    public ResponseDto<PaginationDto<List<TrainingReportDto>>> getReports(User currentUser, Integer unit, Integer page, Integer size) {
        log.info("TrainingWritingServiceImpl.getReports: currentUser = {}, unit = {}, page = {}, size = {}", currentUser.getId(), unit, page, size);

        CommonUtils.checkPageAndSize(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingRecord> trainingRecords;
        if (unit != null) {
            trainingRecords = trainingRecordRepository.findByStudent_IdAndTypeAndTraining_UnitOrderByCreatedAtDesc(currentUser.getId(),
                    TrainingType.WRITING,
                    unit == 1 ? UnitOrder.ONE : UnitOrder.TWO,
                    pageable);
        } else {
            trainingRecords = trainingRecordRepository.findByStudent_IdAndTypeOrderByCreatedAtDesc(currentUser.getId(), TrainingType.WRITING, pageable);
        }

        return ResponseDto.successResponse(PaginationDto.makeForPage(
                trainingRecords.getTotalPages(),
                page,
                size,
                trainingRecords.getTotalElements(),
                trainingRecordMapper.mapToWritingRecordDto(trainingRecords.getContent())
        ));
    }

    private Training findById(UUID id) {
        return trainingRepository.findByIdAndTypeAndDeletedFalse(id, TrainingType.WRITING)
                .orElseThrow(() -> RestException.restThrow("Writing not found", HttpStatus.NOT_FOUND));
    }

    private TrainingRecord findRecordById(UUID id) {
        return trainingRecordRepository.findById(id)
                .orElseThrow(() -> RestException.restThrow("Report not found", HttpStatus.NOT_FOUND));
    }

    @NotNull
    private ResponseDto<TrainingReportDto> submitPrivately(User currentUser, UUID reportId, TrainingWritingSubmitDto request, LocalDateTime endTime) {
        log.info("TrainingWritingServiceImpl.submit: currentUser = {}, reportId = {}, request = {}", currentUser.getId(), reportId, request);
        TrainingRecord record = findRecordById(reportId);
        if (!Objects.equals(currentUser.getId(), record.getStudent().getId())) {
            throw RestException.restThrow("Report not found", HttpStatus.NOT_FOUND);
        }

        if (!Objects.equals(record.getStatus(), TrainingRecordStatus.IN_PROCESS)) {
            throw RestException.restThrow("This training session is not in process");
        }
        record.setStatus(TrainingRecordStatus.COMPLETED);
        record.setEndTime(endTime);
        record.setAnswers(List.of(TrainingAnswer.builder()
                .answer(request.getEssay())
                .build()));
        record = trainingActivityService.saveTrainingRecord(record);

        return ResponseDto.successResponse(trainingRecordMapper.mapToWritingRecordDto(record));
    }
}
