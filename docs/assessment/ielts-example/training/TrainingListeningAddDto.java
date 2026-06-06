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
public class TrainingListeningAddDto {
    @NotBlank(message = "Listening title is expected")
    private String title;
    @NotNull(message = "Listening unit is expected")
    private UnitOrder unit;
    @NotBlank(message = "Listening questions is expected")
    private String questions;
    @NotNull(message = "Audio id is expected")
    private UUID audioId;
}
