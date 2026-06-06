package uz.thompson.appmockielts.payload.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingListeningUpdateDto {
    @NotBlank(message = "Listening title is expected")
    private String title;
    @NotBlank(message = "Listening questions is expected")
    private String questions;
    @NotNull(message = "Audio id is expected")
    private UUID audioId;
}
