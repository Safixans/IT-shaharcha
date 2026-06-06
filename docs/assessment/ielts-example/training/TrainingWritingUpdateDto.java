package uz.thompson.appmockielts.payload.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.enums.UnitOrder;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingWritingUpdateDto {
    @NotBlank(message = "Title is expected")
    private String title;
    @NotBlank(message = "Text is expected")
    private String text;
    @NotNull(message = "Unit number is expected")
    private UnitOrder unit;
    private UUID imageId;
}
