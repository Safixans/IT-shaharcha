package uz.thompson.appmockielts.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.thompson.appmockielts.entity.TrainingRecord;
import uz.thompson.appmockielts.entity.User;
import uz.thompson.appmockielts.exception.RestException;
import uz.thompson.appmockielts.payload.ResponseDto;
import uz.thompson.appmockielts.payload.training.TrainingRecordTitleUpdateDto;
import uz.thompson.appmockielts.repository.TrainingRecordRepository;
import uz.thompson.appmockielts.service.TrainingRecordService;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by dilshodlatipov748@gmail.com on 05/03/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingRecordServiceImpl implements TrainingRecordService {
    private final TrainingRecordRepository trainingRecordRepository;

    @Override
    public ResponseDto<Boolean> updateTitle(User user, TrainingRecordTitleUpdateDto request) {
        log.info("TrainingRecordServiceImpl.updateTitle: user = {}, request = {}", user.getId(), request);
        TrainingRecord record = getById(request.getRecordId());
        if (!Objects.equals(user.getId(), record.getStudent().getId())) {
            throw RestException.restThrow("Training record not found", HttpStatus.NOT_FOUND);
        }

        record.setTitle(request.getTitle());
        trainingRecordRepository.save(record);

        return ResponseDto.successResponse();
    }

    private TrainingRecord getById(UUID id) {
        return trainingRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> RestException.restThrow("Training record not found", HttpStatus.NOT_FOUND));
    }
}
