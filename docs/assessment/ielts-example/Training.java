package uz.thompson.appmockielts.entity;


import jakarta.persistence.*;
import lombok.*;
import uz.thompson.appmockielts.entity.template.AbsUUIDEntity;
import uz.thompson.appmockielts.enums.TrainingType;
import uz.thompson.appmockielts.enums.UnitOrder;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Training extends AbsUUIDEntity {

    @Enumerated(value = EnumType.STRING)
    private TrainingType type;
    @Enumerated(value = EnumType.STRING)
    private UnitOrder unit;
    private String title;
    @Column(nullable = false, length = 50000)
    private String originalSectionData; // obrobotka qilinmagan text
    @Column(nullable = false, length = 50000)
    private String sectionData;  // obrobotka qilingan text
    @Column(length = 50000)
    private String passage;

    private boolean active;
    private Long problemCount;

    private int maxCorrectAnswers;
    private long minTimeSpent;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "training")
    private List<TrainingProblem> trainingProblems;
    @OneToOne
    private Attachment document;


}
