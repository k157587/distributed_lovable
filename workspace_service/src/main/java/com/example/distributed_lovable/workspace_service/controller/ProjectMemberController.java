package com.example.distributed_lovable.workspace_service.controller;

import com.example.distributed_lovable.workspace_service.dto.member.InviteMemberRequest;
import com.example.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.example.distributed_lovable.workspace_service.dto.member.UpdateMemberRoleRequest;
import com.example.distributed_lovable.workspace_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/members")

public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMemberProjects(@PathVariable Long projectId){

        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));

    }

    @PostMapping
    public  ResponseEntity<MemberResponse> inviteMember(
            @PathVariable Long projectId,
            @RequestBody @Valid InviteMemberRequest request
            ){

        return ResponseEntity.status(HttpStatus.CREATED).body(
                projectMemberService.inviteMember(projectId,request)
        );
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestBody @Valid UpdateMemberRoleRequest request
    ){

        return ResponseEntity.ok(projectMemberService.updateMemberRole(projectId,memberId,request));
    }



    @DeleteMapping("/{memberId}")
    public ResponseEntity<MemberResponse> deleteProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId
    ){

        projectMemberService.deleteProjectMember(projectId,memberId);
        return ResponseEntity.noContent().build();
    }
}
