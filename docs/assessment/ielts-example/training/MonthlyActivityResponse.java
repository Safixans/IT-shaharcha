
package uz.thompson.appmockielts.payload.training;

import lombok.*;
import uz.thompson.appmockielts.repository.projection.MonthlyActivityProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyActivityResponse {

    private UUID userId;

    private LocalDate startTime;

    private LocalDate endTime;

    private long totalTests;

    private long totalTimeSpentSeconds;

    private List<MonthlyActivityProjection> dailyActivity;
}