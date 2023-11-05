package com.kusitms.mainservice.domain.team.service;

import com.kusitms.mainservice.domain.roadmap.domain.CustomRoadmap;
import com.kusitms.mainservice.domain.roadmap.domain.CustomRoadmapSpace;
import com.kusitms.mainservice.domain.roadmap.domain.CustomRoadmapTemplate;
import com.kusitms.mainservice.domain.roadmap.domain.RoadmapDownload;
import com.kusitms.mainservice.domain.roadmap.dto.response.CustomRoadmapDetailResponseDto;
import com.kusitms.mainservice.domain.roadmap.dto.response.CustomRoadmapSpaceDetailResponseDto;
import com.kusitms.mainservice.domain.roadmap.repository.RoadmapDownloadRepository;
import com.kusitms.mainservice.domain.team.domain.Team;
import com.kusitms.mainservice.domain.team.domain.TeamSpace;
import com.kusitms.mainservice.domain.team.dto.request.TeamRoadmapRequestDto;
import com.kusitms.mainservice.domain.team.dto.response.TeamRoadmapDetailResponseDto;
import com.kusitms.mainservice.domain.team.dto.response.TeamSpaceResponseDto;
import com.kusitms.mainservice.domain.team.repository.TeamRepository;
import com.kusitms.mainservice.domain.template.dto.response.BaseCustomTemplateResponseDto;
import com.kusitms.mainservice.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.kusitms.mainservice.global.error.ErrorCode.ROADMAP_DOWNLOAD_NOT_FOUND;
import static com.kusitms.mainservice.global.error.ErrorCode.TEAM_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class TeamRoadmapService {
    private final TeamRepository teamRepository;
    private final RoadmapDownloadRepository roadmapDownloadRepository;

    public TeamRoadmapDetailResponseDto getTeamRoadmapDetail(Long teamId) {
        Team team = getTeamFromTeamId(teamId);
        List<TeamSpace> teamSpaceList = team.getTeamSpaceList();
        List<TeamSpaceResponseDto> teamSpaceResponseDtoList = createTeamSpaceResponseDtoList(teamSpaceList);
        CustomRoadmap teamRoadmap = team.getRoadmapDownload().getCustomRoadmap();
        List<CustomRoadmapSpaceDetailResponseDto> teamRoadmapSpaceDetailResponseDtoList
                = createTeamRoadmapSpaceDetailResponseDto(teamRoadmap.getCustomRoadmapSpaceList());
        CustomRoadmapDetailResponseDto teamRoadmapDetailResponseDto
                = CustomRoadmapDetailResponseDto.of(teamRoadmap, teamRoadmapSpaceDetailResponseDtoList);
        return TeamRoadmapDetailResponseDto.of(team, teamSpaceResponseDtoList, teamRoadmapDetailResponseDto);
    }

    public void addTeamRoadmap(Long userId, TeamRoadmapRequestDto teamRoadmapRequestDto) {
        Team team = getTeamFromTeamId(teamRoadmapRequestDto.getTeamId());
        RoadmapDownload roadmapDownload = getRoadmapDownloadFromRoadmapId(teamRoadmapRequestDto.getRoadmapId(), userId);
        addDownloadRoadToTeam(team, roadmapDownload);
    }

    private List<CustomRoadmapSpaceDetailResponseDto> createTeamRoadmapSpaceDetailResponseDto(List<CustomRoadmapSpace> teamRoadmapSpaceList) {
        return teamRoadmapSpaceList.stream()
                .map(customRoadmapSpace ->
                        CustomRoadmapSpaceDetailResponseDto.of(customRoadmapSpace,
                                createBaseCustomTemplateResponseDtoList(customRoadmapSpace)))
                .collect(Collectors.toList());
    }

    private List<TeamSpaceResponseDto> createTeamSpaceResponseDtoList(List<TeamSpace> teamSpaceList) {
        return teamSpaceList.stream()
                .map(TeamSpaceResponseDto::of)
                .collect(Collectors.toList());
    }

    private List<BaseCustomTemplateResponseDto> createBaseCustomTemplateResponseDtoList(CustomRoadmapSpace customRoadmapSpace) {
        List<CustomRoadmapTemplate> customRoadmapTemplateList = customRoadmapSpace.getCustomRoadmapTemplateList();
        return customRoadmapTemplateList.stream()
                .map(customRoadmapTemplate -> BaseCustomTemplateResponseDto.of(customRoadmapTemplate.getCustomTemplate()))
                .collect(Collectors.toList());
    }

    private void addDownloadRoadToTeam(Team team, RoadmapDownload roadmapDownload) {
        team.addRoadmapDownload(roadmapDownload);
    }

    private Team getTeamFromTeamId(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException(TEAM_NOT_FOUND));
    }

    private RoadmapDownload getRoadmapDownloadFromRoadmapId(Long roadmapId, Long userId) {
        return roadmapDownloadRepository.findByRoadmapIdAndUserId(roadmapId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ROADMAP_DOWNLOAD_NOT_FOUND));
    }
}
