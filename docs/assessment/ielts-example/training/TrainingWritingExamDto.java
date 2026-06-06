package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.enums.TrainingRecordStatus;
import uz.thompson.appmockielts.payload.AttachmentDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingWritingExamDto {
    private UUID recordId;
    private TrainingRecordStatus status;
    private String title;
    private String text;
    private AttachmentDto image;

    private LocalDateTime startTime;
    private LocalDateTime deadline;
}
