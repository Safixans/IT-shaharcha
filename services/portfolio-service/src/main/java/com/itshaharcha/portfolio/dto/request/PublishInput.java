package com.itshaharcha.portfolio.dto.request;

import com.itshaharcha.portfolio.entity.Visibility;

/**
 * Publish the portfolio (spec publishPortfolio body). Both optional: handle defaults
 * to a generated one, visibility defaults to public.
 */
public record PublishInput(
        String handle,
        Visibility visibility) {
}
