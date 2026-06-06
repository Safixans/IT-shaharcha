package uz.thompson.appmockielts.payload;

import lombok.*;
import uz.thompson.appmockielts.enums.ProblemType;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDto {
    private UUID id;
    private String orderIndex;
    private String problem;
    private ProblemType problemType;
    private Integer correctAnswers;
    private List<OptionDto> options;
}
