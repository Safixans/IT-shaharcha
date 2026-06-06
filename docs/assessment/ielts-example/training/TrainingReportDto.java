package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.enums.TrainingRecordStatus;
import uz.thompson.appmockielts.enums.TrainingType;
import uz.thompson.appmockielts.payload.mock.AnswerReportDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingReportDto {
    private UUID id;
    private String title;
    private TrainingRecordStatus status;
    private TrainingType type;
    private String essay;
    private int correctAnswers;
    private int incorrectAnswers;
    private int total;
    private List<AnswerReportDto> answers;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
