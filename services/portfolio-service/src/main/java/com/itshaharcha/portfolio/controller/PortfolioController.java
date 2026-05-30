package com.itshaharcha.portfolio.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.portfolio.dto.request.PublishInput;
import com.itshaharcha.portfolio.dto.response.PortfolioResponse;
import com.itshaharcha.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "portfolio", description = "The assembled academic portfolio — publish & view")
@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "Get the caller's assembled academic portfolio")
    @GetMapping("/me")
    public ApiResponse<PortfolioResponse> getMine() {
        return ApiResponse.ok(portfolioService.getMine());
    }

    @Operation(summary = "Publish the portfolio at a public handle")
    @PostMapping("/me:publish")
    public ApiResponse<PortfolioResponse> publish(@RequestBody(required = false) PublishInput input) {
        return ApiResponse.ok(portfolioService.publish(input));
    }

    @Operation(summary = "View a published portfolio by handle (public)")
    @GetMapping("/public/{handle}")
    public ApiResponse<PortfolioResponse> getPublic(@PathVariable String handle) {
        return ApiResponse.ok(portfolioService.getPublic(handle));
    }
}
