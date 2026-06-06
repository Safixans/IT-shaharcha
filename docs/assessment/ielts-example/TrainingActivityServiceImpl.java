package uz.thompson.appmockielts.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.thompson.appmockielts.entity.TrainingRecord;
import uz.thompson.appmockielts.exception.RestException;
import uz.thompson.appmockielts.mapper.TrainingListeningRecordMapper;
import uz.thompson.appmockielts.mapper.TrainingReadingRecordMapper;
import uz.thompson.appmockielts.mapper.TrainingWritingRecordMapper;
import uz.thompson.appmockielts.payload.ResponseDto;
import uz.thompson.appmockielts.payload.training.ActivitySummaryResponse;
import uz.thompson.appmockielts.payload.training.MonthlyActivityResponse;
import uz.thompson.appmockielts.payload.training.SectionSummary;
import uz.thompson.appmockielts.payload.training.TrainingReportDto;
import uz.thompson.appmockielts.repository.TrainingRecordRepository;
import uz.thompson.appmockielts.repository.projection.MonthlyActivityProjection;
import uz.thompson.appmockielts.repository.projection.SectionAggregationProjection;
import uz.thompson.appmockielts.service.TrainingActivityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainingActivityServiceImpl implements TrainingActivityService {

    private final TrainingRecordRepository repository;
    private final TrainingWritingRecordMapper trainingWritingRecordMapper;
    private final TrainingReadingRecordMapper trainingReadingRecordMapper;
    private final TrainingListeningRecordMapper trainingListeningRecordMapper;


    @Cacheable(
            value = "monthlyActivity",
            key = "#userId + '_' + #startTime + '_' + #endTime",
            unless = "#result == null"
    )
    public MonthlyActivityResponse getMonthlyActivity(UUID userId, LocalDate startTime, LocalDate endTime) {
        log.info("TrainingActivityServiceImpl.getMonthlyActivity: userId = {}", userId);

        if (startTime == null || endTime == null) {
            throw RestException.restThrow("Invalid request parameters");
        }

        LocalDateTime start = startTime.atStartOfDay();
        LocalDateTime end = endTime.atStartOfDay();

        List<MonthlyActivityProjection> activity =
                repository.getMonthlyActivity(userId, start, end);

        long totalTests =
                activity.stream()
                        .mapToLong(MonthlyActivityProjection::getTestsCount)
                        .sum();

        long totalTimeSpent =
                activity.stream()
                        .mapToLong(MonthlyActivityProjection::getTimeSpentSeconds)
                        .sum();

        return MonthlyActivityResponse.builder()
                .userId(userId)
                .startTime(startTime)
                .endTime(endTime)
                .totalTests(totalTests)
                .totalTimeSpentSeconds(totalTimeSpent)
                .dailyActivity(activity)
                .build();
    }

    @Transactional
    public TrainingRecord saveTrainingRecord(TrainingRecord record) {
        return repository.save(record);

    }

    @Override
    public List<TrainingReportDto> getBestResultOfStudent(UUID userId) {
        return repository.findBestResultsByStudent(userId)
                .stream()
                .map((record) -> switch (record.getType()) {
                    case READING -> trainingReadingRecordMapper.mapToReadingReportDto(record);
                    case WRITING -> trainingWritingRecordMapper.mapToWritingRecordDto(record);
                    case LISTENING -> trainingListeningRecordMapper.mapToListeningReportDto(record);
                }).toList();
    }

    @Override
    @Cacheable(
            value = "activitySummary",
            key = "#userId + '_' + #startDate + '_' + #endDate",
            unless = "#result == null"
    )
    public ActivitySummaryResponse getActivitySummary(UUID userId,
                                                      LocalDate startDate,
                                                      LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atStartOfDay();

        List<SectionAggregationProjection> result =
                repository.getActivitySummaryAggregated(userId, start, end);

        SectionSummary listening = emptySection();
        SectionSummary reading = emptySection();
        SectionSummary writing = emptySection();

        for (SectionAggregationProjection row : result) {

            SectionSummary section = SectionSummary.builder()
                    .totalCorrectAnswers(
                            Optional.ofNullable(row.getTotalCorrect()).orElse(0)
                    )
                    .totalIncorrectAnswers(
                            Optional.ofNullable(row.getTotalIncorrect()).orElse(0)
                    )
                    .totalTimeSpentSeconds(
                            Optional.ofNullable(row.getTotalTimeSpentSeconds()).orElse(0L)
                    )
                    .build();

            section.setTotal(
                    section.getTotalCorrectAnswers() + section.getTotalIncorrectAnswers()
            );

            switch (row.getType()) {
                case LISTENING -> listening = section;
                case READING -> reading = section;
                case WRITING -> writing = section;
            }
        }

        int overallCorrect =
                listening.getTotalCorrectAnswers()
                        + reading.getTotalCorrectAnswers()
                        + writing.getTotalCorrectAnswers();

        int overallIncorrect =
                listening.getTotalIncorrectAnswers()
                        + reading.getTotalIncorrectAnswers()
                        + writing.getTotalIncorrectAnswers();

        int total = overallCorrect + overallIncorrect;

        long overallTime =
                listening.getTotalTimeSpentSeconds()
                        + reading.getTotalTimeSpentSeconds()
                        + writing.getTotalTimeSpentSeconds();

        return ActivitySummaryResponse.builder()
                .listening(listening)
                .reading(reading)
                .writing(writing)
                .overallCorrectAnswers(overallCorrect)
                .overallIncorrectAnswers(overallIncorrect)
                .total(total)
                .overallTimeSpentSeconds(overallTime)
                .startTime(start)
                .endTime(end)
                .build();
    }

    private SectionSummary emptySection() {
        return SectionSummary.builder()
                .totalCorrectAnswers(0)
                .totalIncorrectAnswers(0)
                .totalTimeSpentSeconds(0L)
                .build();
    }
}