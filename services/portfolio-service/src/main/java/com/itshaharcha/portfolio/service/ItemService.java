package com.itshaharcha.portfolio.service;

import com.itshaharcha.portfolio.dto.request.PortfolioItemCreate;
import com.itshaharcha.portfolio.dto.response.PortfolioItemResponse;
import com.itshaharcha.portfolio.entity.ItemKind;

import java.util.List;
import java.util.UUID;

public interface ItemService {

    List<PortfolioItemResponse> list(ItemKind kind);

    PortfolioItemResponse add(PortfolioItemCreate input);

    void delete(UUID itemId);
}
