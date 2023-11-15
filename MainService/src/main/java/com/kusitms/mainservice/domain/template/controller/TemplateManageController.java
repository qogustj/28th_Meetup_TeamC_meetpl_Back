package com.kusitms.mainservice.domain.template.controller;

import com.kusitms.mainservice.domain.template.dto.request.TemplateReviewRequestDto;
import com.kusitms.mainservice.domain.template.dto.request.TemplateSharingRequestDto;
import com.kusitms.mainservice.domain.template.dto.response.CustomTemplateDetailResponseDto;
import com.kusitms.mainservice.domain.template.dto.response.OriginalTemplateResponseDto;
import com.kusitms.mainservice.domain.template.dto.response.TemplateDownloadDetailResponseDto;
import com.kusitms.mainservice.domain.template.service.TemplateManageService;
import com.kusitms.mainservice.global.common.SuccessResponse;
import com.kusitms.mainservice.global.config.auth.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/manage/template")
@RestController
public class TemplateManageController {
    private final TemplateManageService templateManageService;

    @PostMapping
    public ResponseEntity<SuccessResponse<?>> createSharingTemplate(@UserId final Long userId,
                                                                    @RequestBody final TemplateSharingRequestDto requestDto){
        templateManageService.createSharingTemplate(userId, requestDto);
        return SuccessResponse.created(null);
    }

    @GetMapping("/team")
    public ResponseEntity<SuccessResponse<?>> getTeamTemplateDetailInfo(@UserId final Long userId,
                                                                        @RequestParam final Long templateId,
                                                                        @RequestParam final String roadmapTitle,
                                                                        @RequestParam final String teamTitle) {
        final CustomTemplateDetailResponseDto responseDto = templateManageService.getTeamTemplateDetailInfo(userId, roadmapTitle, teamTitle, templateId);
        return SuccessResponse.ok(responseDto);
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<SuccessResponse<?>> getTemplateDetailInfo(@UserId final Long userId,
                                                                    @PathVariable final Long templateId,
                                                                    @RequestParam final boolean isOpened) {
        final TemplateDownloadDetailResponseDto responseDto;
        if(isOpened) responseDto = templateManageService.getDownloadTemplateDetailInfo(userId, templateId);
        else responseDto = templateManageService.getDownloadCustomTemplateDetailInfo(templateId);
        return SuccessResponse.ok(responseDto);
    }

    @PostMapping("/review")
    public ResponseEntity<SuccessResponse<?>> createTemplateReview(@UserId final Long userId,
                                                                   @RequestBody final TemplateReviewRequestDto requestDto) {
        templateManageService.createTemplateReview(userId, requestDto);
        return SuccessResponse.created(null);
    }
}
