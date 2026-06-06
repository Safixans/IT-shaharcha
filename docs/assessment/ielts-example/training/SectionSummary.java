package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionSummary {

    private int totalCorrectAnswers;
    private int totalIncorrectAnswers;
    private int total;

    private long totalTimeSpentSeconds;
}