package com.itshaharcha.portfolio.service;

import com.itshaharcha.portfolio.dto.response.FileRef;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileRef store(MultipartFile file);
}
