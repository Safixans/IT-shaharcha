package uz.thompson.appmockielts.payload.training;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.utils.RestConstants;

import java.util.UUID;

/**
 * Created by dilshodlatipov748@gmail.com on 05/03/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecordTitleUpdateDto {
    @NotNull(message = "Report id is expected")
    private UUID recordId;
    @NotEmpty(message = "Report title is expected")
    @Pattern(regexp = RestConstants.TITLE_LENGTH, message = "Title length cannot exceed 30 characters")
    private String title;
}
