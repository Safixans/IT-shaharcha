package com.itshaharcha.portfolio.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.portfolio.dto.request.PortfolioItemCreate;
import com.itshaharcha.portfolio.dto.response.PortfolioItemResponse;
import com.itshaharcha.portfolio.entity.ItemKind;
import com.itshaharcha.portfolio.entity.PortfolioItem;
import com.itshaharcha.portfolio.event.PortfolioEventPublisher;
import com.itshaharcha.portfolio.mapper.PortfolioMapper;
import com.itshaharcha.portfolio.repository.PortfolioItemRepository;
import com.itshaharcha.portfolio.security.SecurityUtils;
import com.itshaharcha.portfolio.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final PortfolioItemRepository itemRepository;
    private final PortfolioMapper mapper;
    private final PortfolioEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioItemResponse> list(ItemKind kind) {
        UUID accountId = SecurityUtils.currentAccountId();
        List<PortfolioItem> items = (kind == null)
                ? itemRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                : itemRepository.findByAccountIdAndKindOrderByCreatedAtDesc(accountId, kind);
        return items.stream().map(mapper::toItemResponse).toList();
    }

    @Override
    @Transactional
    public PortfolioItemResponse add(PortfolioItemCreate input) {
        UUID accountId = SecurityUtils.currentAccountId();
        PortfolioItem item = new PortfolioItem();
        item.setAccountId(accountId);
        item.setKind(input.kind());
        item.setTitle(input.title());
        item.setDescription(input.description());
        item.setUrl(input.url());
        item.setFileId(input.fileId());
        item.setTags(input.tags() == null ? new ArrayList<>() : new ArrayList<>(input.tags()));
        PortfolioItem saved = itemRepository.save(item);

        Map<String, Object> data = new HashMap<>();
        data.put("kind", saved.getKind().name());
        data.put("title", saved.getTitle());
        events.emit("portfolio.item.added", "item", saved.getId(), data);
        return mapper.toItemResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID itemId) {
        UUID accountId = SecurityUtils.currentAccountId();
        PortfolioItem item = itemRepository.findByIdAndAccountId(itemId, accountId)
                .orElseThrow(() -> ApplicationException.notFound("Portfolio item not found"));
        itemRepository.delete(item);
    }
}
