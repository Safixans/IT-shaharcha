package uz.thompson.appmockielts.payload.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.utils.RestConstants;

/**
 * Created by dilshodlatipov748@gmail.com on 19/02/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingWritingSubmitDto {
    @NotBlank(message = "Essay is expected")
    @Pattern(regexp = RestConstants.MIN_ESSAY_LENGTH, message = "Essay expected to have at least 50 words")
    private String essay;
}
