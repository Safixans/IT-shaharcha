package uz.thompson.appmockielts.payload.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.thompson.appmockielts.enums.UnitOrder;
import uz.thompson.appmockielts.payload.AttachmentDto;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingWritingDto {
    private UUID id;
    private String title;
    private String text;
    private UnitOrder unit;
    private AttachmentDto image;
}
