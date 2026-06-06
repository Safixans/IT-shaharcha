package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySummaryResponse {

    private SectionSummary listening;
    private SectionSummary reading;
    private SectionSummary writing;

    private int overallCorrectAnswers;
    private int overallIncorrectAnswers;
    private int total;

    private long overallTimeSpentSeconds;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}