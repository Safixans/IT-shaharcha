package uz.thompson.appmockielts.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingAnswer {
    private String answer;
    private List<String> correctOptions;
    private String orderIndex;
    private boolean correct;
}
