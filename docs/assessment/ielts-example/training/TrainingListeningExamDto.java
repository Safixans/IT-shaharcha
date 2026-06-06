package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.payload.AttachmentDto;
import uz.thompson.appmockielts.payload.ProblemDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingListeningExamDto {
    private UUID reportId;
    private String title;
    private String questions;
    private AttachmentDto audio;
    private List<ProblemDto> problems;

    private LocalDateTime startTime;
    private LocalDateTime deadline;
}
