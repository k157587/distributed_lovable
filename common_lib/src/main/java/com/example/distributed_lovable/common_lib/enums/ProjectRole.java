package com.example.distributed_lovable.common_lib.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.example.distributed_lovable.common_lib.enums.ProjectPermission.*;


@RequiredArgsConstructor
@Getter

public enum ProjectRole {
        EDITOR(Set.of(EDIT,VIEW,DELETE,VIEW_MEMBERS)),
    VIEWER(Set.of(VIEW,VIEW_MEMBERS)),
    OWNER(Set.of(VIEW,EDIT,DELETE,MANAGE_MEMBERS,VIEW_MEMBERS));


        ProjectRole(ProjectPermission... permissions){
            this.permission = Set.of(permissions);
        }

    private final Set<ProjectPermission> permission;
}
