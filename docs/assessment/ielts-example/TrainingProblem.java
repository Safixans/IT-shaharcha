package uz.thompson.appmockielts.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.thompson.appmockielts.entity.template.AbsUUIDEntity;
import uz.thompson.appmockielts.entity.template.SectionParent;
import uz.thompson.appmockielts.enums.ProblemType;

import java.util.List;


@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProblem extends AbsUUIDEntity {

    @Enumerated(EnumType.STRING)
    private ProblemType problemType;
    private Integer correctAnswers;
    private String orderIndex;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "problem")
    private List<TrainingOption> options;
    @ManyToOne(fetch = FetchType.LAZY)
    private Training training;
}
