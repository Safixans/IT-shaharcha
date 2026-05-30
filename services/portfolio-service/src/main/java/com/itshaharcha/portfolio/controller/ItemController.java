package com.itshaharcha.portfolio.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.portfolio.dto.request.PortfolioItemCreate;
import com.itshaharcha.portfolio.dto.response.PortfolioItemResponse;
import com.itshaharcha.portfolio.entity.ItemKind;
import com.itshaharcha.portfolio.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "items", description = "Free-form portfolio items (projects, awards, etc.)")
@RestController
@RequestMapping("/api/v1/portfolio/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "List the caller's portfolio items")
    @GetMapping
    public ApiResponse<List<PortfolioItemResponse>> list(
            @RequestParam(required = false) ItemKind kind) {
        return ApiResponse.ok(itemService.list(kind));
    }

    @Operation(summary = "Add a portfolio item")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PortfolioItemResponse> add(@Valid @RequestBody PortfolioItemCreate input) {
        return ApiResponse.ok(itemService.add(input));
    }

    @Operation(summary = "Delete a portfolio item")
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID itemId) {
        itemService.delete(itemId);
    }
}
