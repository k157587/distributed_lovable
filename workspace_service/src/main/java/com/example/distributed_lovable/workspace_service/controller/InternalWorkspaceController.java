package com.example.distributed_lovable.workspace_service.controller;


import com.example.distributed_lovable.common_lib.dto.FileTreeDto;
import com.example.distributed_lovable.common_lib.enums.ProjectPermission;
import com.example.distributed_lovable.workspace_service.service.ProjectFileService;
import com.example.distributed_lovable.workspace_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/internal/v1/")
@RequiredArgsConstructor
public class InternalWorkspaceController {

    private final ProjectFileService projectFileService;
    private final ProjectService projectService;

    @GetMapping("/projects/{projectId}/files/tree")
    public FileTreeDto getFileTree(@PathVariable("projectId") Long projectId){

        return projectFileService.getFileTree(projectId);
    }

    @GetMapping("/projects/{projectId}/files/content")
    public String getFileContent(@PathVariable("projectId") Long projectId, @RequestParam String path){

        return projectFileService.getFileContent(projectId,path);
    }


    @GetMapping("/projects/{projectId}/permissions/check")
    public boolean checkPermission(@PathVariable Long projectId, @RequestParam ProjectPermission projectPermission){

        return projectService.checkPermission(projectId,projectPermission);
    }

}
