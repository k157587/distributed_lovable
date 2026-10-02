package com.example.distributed_lovable.workspace_service.service.impl;

import com.example.distributed_lovable.common_lib.dto.PlanDto;
import com.example.distributed_lovable.common_lib.enums.ProjectPermission;
import com.example.distributed_lovable.common_lib.enums.ProjectRole;
import com.example.distributed_lovable.common_lib.error.BadRequestException;
import com.example.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.example.distributed_lovable.common_lib.security.AuthUtil;
import com.example.distributed_lovable.workspace_service.client.AccountClient;
import com.example.distributed_lovable.workspace_service.dto.project.ProjectRequest;
import com.example.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.example.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;
import com.example.distributed_lovable.workspace_service.entity.Project;
import com.example.distributed_lovable.workspace_service.entity.ProjectMember;
import com.example.distributed_lovable.workspace_service.entity.ProjectMemberId;
import com.example.distributed_lovable.workspace_service.mapper.ProjectMapper;
import com.example.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import com.example.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.example.distributed_lovable.workspace_service.security.SecurityExpressions;
import com.example.distributed_lovable.workspace_service.service.ProjectService;
import com.example.distributed_lovable.workspace_service.service.ProjectTemplateService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    ProjectMapper projectMapper;
    ProjectMemberRepository projectMemberRepository;
    AuthUtil authUtil;
    ProjectTemplateService projectTemplateService;
    AccountClient accountClient;
    SecurityExpressions securityExpressions;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {

        if (!canCreateProject()){
            throw new BadRequestException("user cannot create a new project with current plan,upgrade plan now");
        }

        Long userId = authUtil.getCurrentUserId();
//        User owner = userRepository.findById(userId).orElseThrow(
//                () -> new ResourceNotFoundException("User",userId.toString())
//        );




        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();

        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), userId);

        ProjectMember projectMember = ProjectMember.builder()
                .projectRole(ProjectRole.OWNER)
                .acceptedAt(Instant.now())
                .invitedAt(Instant.now())
                .project(project)
                .id(projectMemberId)
                .build();

        projectTemplateService.initializeProjectFromTemplate(project.getId());

        projectMemberRepository.save(projectMember);


        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {

        Long userId = authUtil.getCurrentUserId();
        var projectsWithRole = projectRepository.findAllAccessibleByUser(userId);

        return projectsWithRole.stream()
                .map(p -> projectMapper.toProjectSummaryResponse(p.getProject(),p.getRole()))
                .toList();
    }

    @Override
    @PreAuthorize("@security.canViewProject(#projectId)")
    public ProjectSummaryResponse getUserProjectById(Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        var projectWithRole = projectRepository.findAccessibleProjectByIdWithRole(projectId,userId)
                .orElseThrow(() -> new BadRequestException("project not found"));
        return projectMapper.toProjectSummaryResponse(projectWithRole.getProject(),projectWithRole.getRole());
    }



    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = projectRepository.findAccessibleProjectById(id,userId).orElseThrow();




        project.setName(request.name());

        project = projectRepository.save(project);

        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#id)")
    public void softDelete(Long id) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(id,userId);


        project.setDeletedAt(Instant.now());

        projectRepository.save(project);
    }

    @Override
    public boolean checkPermission(Long projectId, ProjectPermission projectPermission) {

        return securityExpressions.hasPermission(projectId,projectPermission);

    }


    // INTERNAL FUNCTION

    public Project getAccessibleProjectById(Long projectId,Long userId){
        return projectRepository.findAccessibleProjectById(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId.toString()));


    }

    public boolean canCreateProject(){
        Long userId = authUtil.getCurrentUserId();

        if (userId == null) return false;

        PlanDto planDto = accountClient.getCurrentSubscriptionPlan();

        int maxAllowed = planDto.maxProjects();;
        int ownedcount = projectMemberRepository.countProjectOwnedByUser(userId);

        return ownedcount < maxAllowed;
    }
}
