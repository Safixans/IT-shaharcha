package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.request.DocInput;
import com.itshaharcha.learning.dto.request.DocReadInput;
import com.itshaharcha.learning.dto.response.DocResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DocService {

    PageResponse<DocResponse> listDocs(String topic, Pageable pageable);

    void recordRead(UUID docId, DocReadInput input);

    DocResponse createDoc(DocInput input);

    DocResponse updateDoc(UUID docId, DocInput input);

    void deleteDoc(UUID docId);
}
