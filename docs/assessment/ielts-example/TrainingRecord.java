package uz.thompson.appmockielts.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import uz.thompson.appmockielts.entity.template.AbsUUIDEntity;
import uz.thompson.appmockielts.enums.TrainingRecordStatus;
import uz.thompson.appmockielts.enums.TrainingType;

import java.time.LocalDateTime;
import java.util.List;

import static org.hibernate.type.SqlTypes.JSON;

/**
 * Created by dilshodlatipov748@gmail.com on 19/02/2026
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecord extends AbsUUIDEntity {
    @ManyToOne
    private User student;
    @ManyToOne
    private Training training;
    @Enumerated(EnumType.STRING)
    private TrainingType type;
    @Enumerated(EnumType.STRING)
    private TrainingRecordStatus status;
    private String title;
    private int correctAnswers;
    private int incorrectAnswers;
    @JdbcTypeCode(value = JSON)
    @Column(columnDefinition = "jsonb")
    private List<TrainingAnswer> answers;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime deadline;
}
