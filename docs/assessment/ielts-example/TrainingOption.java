package uz.thompson.appmockielts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.thompson.appmockielts.entity.template.AbsUUIDEntity;


@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TrainingOption extends AbsUUIDEntity {
    @Column(nullable = false)
    private String option;
    @Column(nullable = false)
    private Boolean correct;
    @Column(nullable = false)
    private String orderIndex;
    @ManyToOne(optional = false)
    private TrainingProblem problem;
}
