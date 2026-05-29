package com.itshaharcha.learning.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.learning.dto.request.DocInput;
import com.itshaharcha.learning.dto.request.DocReadInput;
import com.itshaharcha.learning.dto.response.DocResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.entity.Doc;
import com.itshaharcha.learning.entity.DocRead;
import com.itshaharcha.learning.event.LearningEventPublisher;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.DocReadRepository;
import com.itshaharcha.learning.repository.DocRepository;
import com.itshaharcha.learning.security.SecurityUtils;
import com.itshaharcha.learning.service.DocService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocServiceImpl implements DocService {

    private final DocRepository docRepository;
    private final DocReadRepository docReadRepository;
    private final LearningMapper mapper;
    private final LearningEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocResponse> listDocs(String topic, Pageable pageable) {
        return PageResponse.from(docRepository.search(topic, pageable), mapper::toDocResponse);
    }

    @Override
    @Transactional
    public void recordRead(UUID docId, DocReadInput input) {
        if (!docRepository.existsById(docId)) {
            throw ApplicationException.notFound("Doc not found");
        }
        UUID accountId = SecurityUtils.currentAccountId();
        DocRead read = docReadRepository.findByDocIdAndAccountId(docId, accountId)
                .orElseGet(() -> {
                    DocRead r = new DocRead();
                    r.setDocId(docId);
                    r.setAccountId(accountId);
                    return r;
                });
        if (input != null) {
            read.setDurationSeconds(input.durationSeconds());
            read.setScrollPercent(input.scrollPercent());
        }
        docReadRepository.save(read);
        Map<String, Object> data = new HashMap<>();
        data.put("durationSeconds", read.getDurationSeconds());
        data.put("scrollPercent", read.getScrollPercent());
        events.emit("learning.doc.read", "doc", docId, data);
    }

    @Override
    @Transactional
    public DocResponse createDoc(DocInput input) {
        Doc doc = new Doc();
        apply(doc, input);
        return mapper.toDocResponse(docRepository.save(doc));
    }

    @Override
    @Transactional
    public DocResponse updateDoc(UUID docId, DocInput input) {
        Doc doc = require(docId);
        apply(doc, input);
        return mapper.toDocResponse(docRepository.save(doc));
    }

    @Override
    @Transactional
    public void deleteDoc(UUID docId) {
        docRepository.delete(require(docId));
    }

    private void apply(Doc doc, DocInput input) {
        doc.setTitle(input.title());
        doc.setTopic(input.topic());
        doc.setUrl(input.url());
        doc.setBody(input.body());
        doc.setEstimatedMinutes(input.estimatedMinutes());
        doc.setSourceId(input.sourceId());
    }

    private Doc require(UUID id) {
        return docRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Doc not found"));
    }
}
