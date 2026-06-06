package uz.thompson.appmockielts.service.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uz.thompson.appmockielts.entity.Training;
import uz.thompson.appmockielts.enums.TrainingType;
import uz.thompson.appmockielts.exception.RestException;
import uz.thompson.appmockielts.mapper.TrainingMapper;
import uz.thompson.appmockielts.payload.PaginationDto;
import uz.thompson.appmockielts.payload.ResponseDto;
import uz.thompson.appmockielts.payload.training.TrainingInfoDto;
import uz.thompson.appmockielts.repository.TrainingRepository;
import uz.thompson.appmockielts.service.TrainingInfoService;
import uz.thompson.appmockielts.utils.CommonUtils;

import java.util.List;

/**
 * Created by dilshodlatipov748@gmail.com on 10/03/2026
 */
@Service
@RequiredArgsConstructor
public class TrainingInfoServiceImpl implements TrainingInfoService {
    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;

    @Override
    public ResponseDto<PaginationDto<List<TrainingInfoDto>>> search(TrainingType type, Integer page, Integer size) {
        if (type == null) {
            throw RestException.restThrow("Training type is expected");
        }
        CommonUtils.checkPageAndSize(page, size);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Training> trainings = trainingRepository.findAllByTypeAndDeletedFalseAndActiveTrueOrderByCreatedAtDesc(type, pageRequest);

        return ResponseDto.successResponse(
                PaginationDto.makeForPage(trainings.getTotalPages(),
                        page,
                        size,
                        trainings.getTotalElements(),
                        trainingMapper.mapToTrainingInfoDto(trainings.getContent()))
        );
    }
}
