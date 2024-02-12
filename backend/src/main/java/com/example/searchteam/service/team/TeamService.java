package com.example.searchteam.service.team;

import com.example.searchteam.dto.request.team.TeamAddRequest;
import com.example.searchteam.dto.request.team.TeamFiltrationRequest;
import com.example.searchteam.dto.request.team.TeamRequest;
import com.example.searchteam.dto.response.team.ShortTeamResponse;
import com.example.searchteam.dto.response.team.TeamResponse;
import com.example.searchteam.dto.response.team_member.TeamMemberResponse;
import com.example.searchteam.service.domain.team.TeamDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

/**
 * Сервис команд
 * @deprecated реализует методы обработки информации о команде
 */
public class TeamService {

    /**
     * Domain Service команд
     * @deprecated реализует методы обработки информации о команде
     */
    private final TeamDomainService service;

    /**
     * получение команды по id
     * @param request - id
     */
    public TeamResponse getTeamById(TeamRequest request) {
        return service.getTeamById(request.getTeamId());
    }

    /**
     * получение всех команд
     */
    public List<TeamResponse> getAllTeams() {
        return service.getAllTeams();
    }

    /**
     * Создание новой команды
     * @param request - TeamAddRequest(id,name,description,userId,typeTeamId,members)
     */
    public TeamResponse addTeam(TeamAddRequest request) {
        Long teamId = service.addTeam(request);
        return service.getTeamById(teamId);
    }

    /**
     * Изменение команды
     * @param request - TeamAddRequest(id,name,description,userId,typeTeamId,members)
     */
    public TeamResponse editTeam(TeamAddRequest request) {
        Long teamId = service.editTeam(request);
        return service.getTeamById(teamId);
    }

    /**
     * Удаление команды
     * @param request - TeamRequest(teamId)
     */
    public void deleteTeam(TeamRequest request) {
        service.deleteTeamById(request.getTeamId());
    }

    /**
     * Поиск команд
     * @param request - TeamFiltrationRequest(teamTypes,isCompleted,name,start,finish,users,members)
     */
    public List<ShortTeamResponse> getSearchTeams(TeamFiltrationRequest request) {
        return service.getAllTeams().stream()
                .filter(t -> applyFilter(t,request))
                .map(t ->new ShortTeamResponse(t.getTeamId(),t.getName(),t.getUser()))
                .toList();

    }

    /**
     * Фильтр для поиска команд
     * @param source - TeamResponse(teamId,name,description,user,isComleted,typeTeam,teamMemberType,members), TeamFiltrationRequest(teamTypes,isCompleted,name,start,finish,users,members)
     */
    private boolean applyFilter(TeamResponse source, TeamFiltrationRequest filter) {

        if (filter.getStart() == null && filter.getFinish() == null && filter.getName() == null
                && filter.getIsCompleted() == null && filter.getTeamTypes().isEmpty() && filter.getUsers().isEmpty() && filter.getMembers().isEmpty()
        ) return true;


        if (filter.getStart() != null && (filter.getStart().isAfter(source.getCreated())))
            return false;

        if (filter.getFinish() != null && (filter.getFinish().isBefore(source.getCreated())))
            return false;

        if (filter.getIsCompleted() != null && !filter.getIsCompleted().equals(source.isCompleted()))
            return false;

        if (filter.getName() != null && !source.getName().toLowerCase().contains(filter.getName().toLowerCase())){
            return false;
        }

        var userFilter = filter
                .getUsers()
                .stream()
                .noneMatch(u -> source.getUser().getFullName().toLowerCase().contains(u.toLowerCase()));


        if (!filter.getUsers().isEmpty() && userFilter) {
            return false;
        }

        var typeFilter = filter.getTeamTypes()
                .stream()
                .noneMatch(t -> source.getTypeTeam().getName().toLowerCase().contains(t.toLowerCase()));



        if (!filter.getTeamTypes().isEmpty() && typeFilter) {
            return false;
        }

        boolean memberFilter = false;

        for (TeamMemberResponse member : source.getMembers()) {

            var name = member.getName().toLowerCase();
            var mFilter = filter.getMembers().stream().anyMatch(m -> name.contains(m.toLowerCase()));
            if (mFilter) {
                memberFilter = true;
                break;
            }
        }


        return filter.getMembers().isEmpty() || !memberFilter;
    }
}
