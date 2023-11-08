package com.kusitms.mainservice.domain.template.service;

import com.kusitms.mainservice.domain.roadmap.domain.CustomRoadmap;
import com.kusitms.mainservice.domain.roadmap.domain.CustomRoadmapSpace;
import com.kusitms.mainservice.domain.roadmap.domain.Roadmap;
import com.kusitms.mainservice.domain.roadmap.dto.response.BaseRoadmapResponseDto;
import com.kusitms.mainservice.domain.roadmap.dto.response.CustomRoadmapDetailResponseDto;
import com.kusitms.mainservice.domain.roadmap.dto.response.CustomRoadmapSpaceDetailResponseDto;
import com.kusitms.mainservice.domain.roadmap.dto.response.RoadmapDetailResponseDto;
import com.kusitms.mainservice.domain.roadmap.repository.CustomRoadmapRepository;
import com.kusitms.mainservice.domain.roadmap.repository.RoadmapRepository;
import com.kusitms.mainservice.domain.team.domain.Team;
import com.kusitms.mainservice.domain.team.dto.response.TeamResponseDto;
import com.kusitms.mainservice.domain.team.dto.response.TeamSpaceResponseDto;
import com.kusitms.mainservice.domain.team.repository.TeamRepository;
import com.kusitms.mainservice.domain.template.domain.*;
import com.kusitms.mainservice.domain.template.dto.request.TemplateReviewRequestDto;
import com.kusitms.mainservice.domain.template.dto.response.BaseCustomTemplateResponseDto;
import com.kusitms.mainservice.domain.template.dto.response.CustomTemplateDetailResponseDto;
import com.kusitms.mainservice.domain.template.dto.response.OriginalTemplateResponseDto;
import com.kusitms.mainservice.domain.template.dto.response.TemplateDownloadDetailResponseDto;
import com.kusitms.mainservice.domain.template.repository.*;
import com.kusitms.mainservice.domain.user.domain.User;
import com.kusitms.mainservice.domain.user.dto.response.MakerResponseDto;
import com.kusitms.mainservice.domain.user.repository.UserRepository;
import com.kusitms.mainservice.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kusitms.mainservice.global.error.ErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TemplateManageService {
    private final TemplateRepository templateRepository;
    private final TemplateContentRepository teamContentRepository;
    private final CustomTemplateRepository customTemplateRepository;
    private final CustomRoadmapRepository customRoadmapRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TemplateDownloadRepository templateDownloadRepository;
    private final TemplateReviewRepository templateReviewRepository;
    private final RoadmapRepository roadmapRepository;
    private final ReviewerRepository reviewerRepository;

    public OriginalTemplateResponseDto getOriginalTemplateInfo(Long userId, Long templateId){
        Template template = getTemplateFromTemplateId(templateId);
        TemplateContent templateContent = getTemplateContentFromTemplateId(templateId);
        Roadmap roadmap = getRoadmapFromUserIdAndTemplateId(userId, templateId);
        BaseRoadmapResponseDto baseRoadmapResponseDto = createBaseRoadmapResponseDto(roadmap);
        MakerResponseDto makerResponseDto = createMakerResponseDto(template, userId);
        return OriginalTemplateResponseDto.of(template, templateContent.getContent(), baseRoadmapResponseDto, makerResponseDto);
    }

    public CustomTemplateDetailResponseDto getTeamTemplateDetailInfo(Long userId, String roadmapTitle, String teamTitle, Long templateId) {
        CustomTemplate customTemplate = getCustomTemplateFromTemplateId(templateId);
        TemplateContent templateContent = getTemplateContentFromTemplateId(templateId);
        CustomRoadmap relatedRoadmap = getCustomRoadmapFromUserIdAndTitle(userId, roadmapTitle);
        CustomRoadmapDetailResponseDto baseRoadmapResponseDto = createCustomRoadmapDetailResponseDto(relatedRoadmap);
        Team team = getTeamFromTitleAndUserId(userId, teamTitle);
        TeamResponseDto teamResponseDto = createTeamResponseDto(team);
        return CustomTemplateDetailResponseDto.of(customTemplate, templateContent.getContent(), baseRoadmapResponseDto, teamResponseDto);
    }

    public TemplateDownloadDetailResponseDto getDownloadTemplateDetailInfo(Long userId, Long templateId) {
        Template template = getTemplateFromTemplateId(templateId);
        TemplateContent templateContent = getTemplateContentFromTemplateId(templateId);
        TemplateDownload templateDownload = getTemplateDownloadFromUserIdAndTemplateId(userId, templateId);
        return TemplateDownloadDetailResponseDto.ofTemplate(template, templateDownload.getCustomTemplate().getId(), templateContent.getContent());
    }

    public TemplateDownloadDetailResponseDto getDownloadCustomTemplateDetailInfo(Long templateId) {
        CustomTemplate customTemplate = getCustomTemplateFromTemplateId(templateId);
        TemplateContent templateContent = getTemplateContentFromTemplateId(templateId);
        Long relatedTemplateId = customTemplate.getTemplateDownload().getId();
        return TemplateDownloadDetailResponseDto.ofCustomTemplate(customTemplate, relatedTemplateId, templateContent.getContent());
    }

    public void createTemplateReview(Long userId, TemplateReviewRequestDto templateReviewRequestDto){
        User user = getUserFromUserId(userId);
        Template template = getTemplateFromTemplateId(templateReviewRequestDto.getMeetingId());
        TemplateReview createdTemplateReview = TemplateReview.createTemplateReview(templateReviewRequestDto.getContent());
        Reviewer createdReviewer = Reviewer.createReviewer(user, createdTemplateReview, template);
        saveReviewer(createdReviewer);
        saveTemplateReview(createdTemplateReview);
    }


    private MakerResponseDto createMakerResponseDto(Template template, Long sessionId){
        User maker = template.getUser();
        int templateCount = getCreatedTemplateCount(maker.getId());
        int roadmapCount = getCreatedRoadmapCount(maker.getId());
        return MakerResponseDto.of(maker.getName(), templateCount, roadmapCount, sessionId);
    }

    private BaseRoadmapResponseDto createBaseRoadmapResponseDto(Roadmap roadmap) {
        if(Objects.isNull(roadmap)) return null;
        List<RoadmapDetailResponseDto> roadmapDetailResponseDtoList = RoadmapDetailResponseDto.listOf(roadmap);
        return BaseRoadmapResponseDto.of(roadmap, roadmapDetailResponseDtoList);
    }

    private TeamResponseDto createTeamResponseDto(Team team) {
        List<TeamSpaceResponseDto> teamSpaceResponseDtoList = TeamSpaceResponseDto.listOf(team.getTeamSpaceList());
        return TeamResponseDto.of(team, teamSpaceResponseDtoList);
    }

    private CustomRoadmapDetailResponseDto createCustomRoadmapDetailResponseDto(CustomRoadmap relatedRoadmap) {
        List<CustomRoadmapSpaceDetailResponseDto> customRoadmapSpaceDetailList
                = createTeamRoadmapSpaceDetailResponseDto(relatedRoadmap.getCustomRoadmapSpaceList());
        return CustomRoadmapDetailResponseDto.of(relatedRoadmap, customRoadmapSpaceDetailList);
    }

    private List<CustomRoadmapSpaceDetailResponseDto> createTeamRoadmapSpaceDetailResponseDto(List<CustomRoadmapSpace> teamRoadmapSpaceList) {
        return teamRoadmapSpaceList.stream()
                .map(customRoadmapSpace ->
                        CustomRoadmapSpaceDetailResponseDto.of(customRoadmapSpace, BaseCustomTemplateResponseDto.listOf(customRoadmapSpace)))
                .collect(Collectors.toList());
    }

    private int getCreatedTemplateCount(Long userId){
        return templateRepository.countByUserId(userId);
    }

    private int getCreatedRoadmapCount(Long userId){
        return roadmapRepository.countByUserId(userId);
    }

    private Roadmap getRoadmapFromUserIdAndTemplateId(Long userId, Long templateId){
        return roadmapRepository.findRoadmapByMakerIdAndTemplateId(userId, templateId)
                .orElse(null);
    }

    private TemplateDownload getTemplateDownloadFromUserIdAndTemplateId(Long userId, Long templateId){
        return templateDownloadRepository.findByUserIdAndTemplateId(userId, templateId)
                .orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
    }

    private Team getTeamFromTitleAndUserId(Long userId, String title) {
        return teamRepository.findByUserIdAndTitle(userId, title)
                .orElseThrow(() -> new EntityNotFoundException(TEAM_NOT_FOUND));
    }

    private CustomRoadmap getCustomRoadmapFromUserIdAndTitle(Long userId, String title) {
        return customRoadmapRepository.findByUserIdAndTitle(userId, title)
                .orElseThrow(() -> new EntityNotFoundException(ROADMAP_NOT_FOUND));
    }

    private CustomTemplate getCustomTemplateFromTemplateId(Long templateId) {
        return customTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
    }

    private TemplateContent getTemplateContentFromTemplateId(Long templateId) {
        return teamContentRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new EntityNotFoundException(TEMPLATE_CONTENT_NOT_FOUND));
    }

    private Template getTemplateFromTemplateId(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
    }

    private User getUserFromUserId(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }

    private void saveReviewer(Reviewer reviewer){
        reviewerRepository.save(reviewer);
    }

    private void saveTemplateReview(TemplateReview templateReview){
        templateReviewRepository.save(templateReview);
    }
}
