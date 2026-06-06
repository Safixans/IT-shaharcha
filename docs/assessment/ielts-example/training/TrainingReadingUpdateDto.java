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
public class TrainingReadingUpdateDto {
    @NotBlank(message = "Reading title is expected")
    private String title;
    @NotBlank(message = "Reading questions is expected")
    private String questions;
    @NotBlank(message = "Reading passage is expected")
    private String passage;
    @NotNull(message = "Document id is expected")
    private UUID documentId;
}
