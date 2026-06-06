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
public class TrainingListeningDto {
    private UUID id;
    private String title;
    private Boolean active;
    private AttachmentDto audio;
    private String questions;
    private String originalQuestions;
    private UnitOrder unit;
    private List<ProblemDto> problems;
    private Long problemCount;
}
