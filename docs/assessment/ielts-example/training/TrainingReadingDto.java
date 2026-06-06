package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.enums.UnitOrder;
import uz.thompson.appmockielts.payload.AttachmentDto;
import uz.thompson.appmockielts.payload.ProblemDto;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingReadingDto {
    private UUID id;
    private String title;
    private String questions;
    private String originalQuestions;
    private String passage;
    private Boolean active;
    private UnitOrder unit;
    private AttachmentDto document;
    private List<ProblemDto> problems;
    private Long problemCount;
}
