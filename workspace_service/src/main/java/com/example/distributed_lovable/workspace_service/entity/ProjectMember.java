package com.example.distributed_lovable.workspace_service.entity;

import com.example.distributed_lovable.common_lib.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor

@Entity
@NoArgsConstructor
@Builder
@Table(name = "project_members")

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMember {

    @EmbeddedId
    ProjectMemberId id;

    @ManyToOne
            @MapsId("projectId")
    Project project;



    @Enumerated(EnumType.STRING)
            @Column(nullable = false)
    ProjectRole projectRole;

    Instant invitedAt;
    Instant acceptedAt;
}
