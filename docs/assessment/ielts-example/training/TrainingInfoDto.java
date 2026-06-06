package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by dilshodlatipov748@gmail.com on 10/03/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingInfoDto {
    private String id;
    private String title;
}
