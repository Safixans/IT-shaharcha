package com.itshaharcha.portfolio.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.portfolio.dto.response.FileRef;
import com.itshaharcha.portfolio.entity.StoredFile;
import com.itshaharcha.portfolio.repository.FileRepository;
import com.itshaharcha.portfolio.security.SecurityUtils;
import com.itshaharcha.portfolio.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    @Transactional
    public FileRef store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApplicationException.badRequest("File must not be empty");
        }
        StoredFile stored = new StoredFile();
        stored.setAccountId(SecurityUtils.currentAccountId());
        stored.setOriginalName(file.getOriginalFilename());
        stored.setContentType(file.getContentType() != null
                ? file.getContentType() : "application/octet-stream");
        stored.setSizeBytes(file.getSize());
        try {
            stored.setData(file.getBytes());
        } catch (IOException ex) {
            throw ApplicationException.badRequest("Could not read uploaded file");
        }
        StoredFile saved = fileRepository.save(stored);
        return new FileRef(saved.getId(), saved.getContentType(), saved.getSizeBytes(), null);
    }
}
