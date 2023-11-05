package com.kusitms.mainservice.domain.team.service;

import com.kusitms.mainservice.domain.team.domain.Team;
import com.kusitms.mainservice.domain.team.domain.TeamSpace;
import com.kusitms.mainservice.domain.team.domain.TeamType;
import com.kusitms.mainservice.domain.team.dto.request.TeamRequestDto;
import com.kusitms.mainservice.domain.team.dto.request.TeamSpaceRequestDto;
import com.kusitms.mainservice.domain.team.dto.request.UpdateTeamRequestDto;
import com.kusitms.mainservice.domain.team.dto.response.TeamResponseDto;
import com.kusitms.mainservice.domain.team.dto.response.TeamSpaceResponseDto;
import com.kusitms.mainservice.domain.team.repository.TeamRepository;
import com.kusitms.mainservice.domain.user.domain.User;
import com.kusitms.mainservice.domain.user.repository.UserRepository;
import com.kusitms.mainservice.global.error.exception.ConflictException;
import com.kusitms.mainservice.global.error.exception.EntityNotFoundException;
import com.kusitms.mainservice.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kusitms.mainservice.domain.team.domain.TeamSpaceType.getEnumSpaceTypeFromStringSpaceType;
import static com.kusitms.mainservice.domain.team.domain.TeamType.getEnumTeamTypeFromStringTeamType;
import static com.kusitms.mainservice.global.error.ErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class TeamService {
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final static int MAX_SPACE_SIZE = 3;

    public TeamResponseDto createTeam(Long userId, TeamRequestDto teamRequestDto) {
        validateDuplicateTeamTitle(teamRequestDto.getTitle());
        validateTeamSpaceSize(teamRequestDto.getSpaceList());
        User user = getUserFromUserId(userId);
        Team createTeam = createTeamFromRequestDto(teamRequestDto, user);
        List<TeamSpace> createTeamSpace = createTeamSpaceFromRequestDto(teamRequestDto.getSpaceList(), createTeam);
        List<TeamSpaceResponseDto> teamSpaceResponseDtoList = createTeamSpaceResponseDtoList(createTeamSpace);
        return TeamResponseDto.of(createTeam, teamSpaceResponseDtoList);
    }

    public TeamResponseDto updateTeamInfo(UpdateTeamRequestDto updateTeamRequestDto) {
        validateDuplicateTeamTitleWithNull(updateTeamRequestDto.getTitle());
        validateTeamSpaceSize(updateTeamRequestDto.getSpaceList());
        Team team = getTeamFromTeamId(updateTeamRequestDto.getTeamId());
        team.updateTeamInfo(updateTeamRequestDto.getTitle(), updateTeamRequestDto.getTeamType(), updateTeamRequestDto.getIntroduction());
        List<TeamSpace> teamSpaceList = updateTeamSpace(updateTeamRequestDto.getSpaceList(), team);
        List<TeamSpaceResponseDto> teamSpaceResponseDtoList = createTeamSpaceResponseDtoList(teamSpaceList);
        return TeamResponseDto.of(team, teamSpaceResponseDtoList);
    }

    private List<TeamSpaceResponseDto> createTeamSpaceResponseDtoList(List<TeamSpace> teamSpaceList) {
        return teamSpaceList.stream()
                .map(TeamSpaceResponseDto::of)
                .collect(Collectors.toList());
    }

    private List<TeamSpace> createTeamSpaceFromRequestDto(List<TeamSpaceRequestDto> requestDtoList, Team createTeam) {
        return requestDtoList.stream()
                .map(teamSpaceRequestDto ->
                        TeamSpace.createTeamSpace(teamSpaceRequestDto.getUrl(),
                                getEnumSpaceTypeFromStringSpaceType(teamSpaceRequestDto.getSpaceType()),
                                createTeam))
                .collect(Collectors.toList());
    }

    private List<TeamSpace> updateTeamSpace(List<TeamSpaceRequestDto> spaceList, Team team) {
        team.resetTeamSpaceList();
        return createTeamSpaceFromRequestDto(spaceList, team);
    }

    private Team createTeamFromRequestDto(TeamRequestDto teamRequestDto, User user) {
        TeamType teamType = getEnumTeamTypeFromStringTeamType(teamRequestDto.getTeamType());
        return Team.createTeam(teamRequestDto.getTitle(), teamType, teamRequestDto.getIntroduction(), user);
    }

    private Team getTeamFromTeamId(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException(TEAM_NOT_FOUND));
    }

    private User getUserFromUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }

    private void validateTeamSpaceSize(List<TeamSpaceRequestDto> requestDtoList) {
        if (Objects.isNull(requestDtoList))
            throw new InvalidValueException(EMPTY_TEAM_SPACE);
        if (requestDtoList.size() > MAX_SPACE_SIZE)
            throw new InvalidValueException(INVALID_TEAM_SPACE_SIZE);
    }

    private void validateDuplicateTeamTitleWithNull(String title) {
        if (Objects.isNull(title)) return;
        if (teamRepository.existsTeamByTitle(title))
            throw new ConflictException(DUPLICATE_TEAM);
    }

    private void validateDuplicateTeamTitle(String title) {
        if (teamRepository.existsTeamByTitle(title))
            throw new ConflictException(DUPLICATE_TEAM);
    }
}
