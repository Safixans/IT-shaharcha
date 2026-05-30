package com.itshaharcha.portfolio.service;

import com.itshaharcha.portfolio.dto.request.PublishInput;
import com.itshaharcha.portfolio.dto.response.PortfolioResponse;

public interface PortfolioService {

    PortfolioResponse getMine();

    PortfolioResponse publish(PublishInput input);

    PortfolioResponse getPublic(String handle);
}
