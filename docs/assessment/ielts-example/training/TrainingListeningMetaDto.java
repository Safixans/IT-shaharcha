package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingListeningMetaDto {
    private String id;
    private String title;
    private int unit;
    private int maxCorrectAnswers;
    private long minTimeSpent;
    private long length;
    private Long problemCount;
    private boolean active;
}
