package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Created by dilshodlatipov748@gmail.com on 19/02/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingWritingMetaDto {
    private UUID id;
    private String title;
    private int unit;
    private boolean active;
}
