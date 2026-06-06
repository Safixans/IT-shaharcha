package uz.thompson.appmockielts.payload;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionDto {
    private UUID id;
    private String orderIndex;
    private String option;
}
