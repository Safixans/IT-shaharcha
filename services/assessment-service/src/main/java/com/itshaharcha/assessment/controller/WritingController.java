package com.itshaharcha.assessment.controller;

import com.itshaharcha.assessment.dto.request.ActivationRequest;
import com.itshaharcha.assessment.dto.request.WritingCreate;
import com.itshaharcha.assessment.dto.response.AttemptSession;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.dto.response.UnitDetail;
import com.itshaharcha.assessment.dto.response.UnitMeta;
import com.itshaharcha.assessment.entity.IeltsSkill;
import com.itshaharcha.assessment.security.SecurityUtils;
import com.itshaharcha.assessment.service.AttemptService;
import com.itshaharcha.assessment.service.IeltsUnitService;
import com.itshaharcha.common.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/v1/assessment/ielts/writing")
@RequiredArgsConstructor
public class WritingController {

    private final IeltsUnitService units;
    private final AttemptService attempts;

    @GetMapping
    public ApiResponse<PageResponse<UnitMeta>> list(
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(units.browse(IeltsSkill.WRITING, !SecurityUtils.isAuthor(), tags,
                PageRequest.of(page, size)));
    }

    @GetMapping("/{unitId}")
    public ApiResponse<UnitDetail> get(@PathVariable UUID unitId) {
        return ApiResponse.ok(units.get(unitId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','TEACHER')")
    public ApiResponse<UnitDetail> create(@Valid @RequestBody WritingCreate body) {
        return ApiResponse.ok(units.createWriting(body));
    }

    @DeleteMapping("/{unitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public void delete(@PathVariable UUID unitId) {
        units.delete(unitId);
    }

    @PostMapping("/{unitId}:activate")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','TEACHER')")
    public ApiResponse<UnitDetail> activate(@PathVariable UUID unitId,
                                            @RequestBody(required = false) ActivationRequest body) {
        return ApiResponse.ok(units.setActive(unitId, body == null || body.active() == null || body.active()));
    }

    @PostMapping("/{unitId}:start")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AttemptSession> start(@PathVariable UUID unitId) {
        return ApiResponse.ok(attempts.start(unitId));
    }
}
