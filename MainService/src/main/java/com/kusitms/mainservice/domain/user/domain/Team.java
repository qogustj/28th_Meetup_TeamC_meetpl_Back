package com.kusitms.mainservice.domain.user.domain;

import com.kusitms.mainservice.domain.roadmap.domain.Roadmap;
import com.kusitms.mainservice.domain.roadmap.domain.RoadmapDownload;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.kusitms.mainservice.domain.user.domain.TeamType.getEnumTeamTypeFromStringTeamType;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "team")
@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;
    private String title;
    @Enumerated(EnumType.STRING)
    private TeamType teamType;
    private String introduction;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne
    private RoadmapDownload roadmapDownload;
    @OneToMany(mappedBy = "team")
    @Builder.Default
    private List<TeamSpace> teamSpaceList = new ArrayList<>();

    public static Team createTeam(String title, TeamType teamType, String introduction, User user) {
        Team team = Team.builder()
                .title(title)
                .teamType(teamType)
                .introduction(introduction)
                .user(user)
                .build();
        user.updateTeamList(team);
        return team;
    }
    public void addTeamSpaceList(TeamSpace teamSpace){
        this.teamSpaceList.add(teamSpace);
    }
    public void updateTeamInfo(String title, String teamType, String introduction) {
        this.title = Objects.isNull(title) ? this.title : title;
        this.teamType = Objects.isNull(teamType) ? this.teamType : getEnumTeamTypeFromStringTeamType(teamType);
        this.introduction = Objects.isNull(introduction) ? this.introduction : introduction;
    }
    public void resetTeamSpaceList(){
        this.teamSpaceList = new ArrayList<>();
    }
}
