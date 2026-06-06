package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.payload.mock.AnswerDto;

import java.util.List;

/**
 * Created by dilshodlatipov748@gmail.com on 19/02/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingReadingSubmitDto {
    List<AnswerDto> answers;
}
