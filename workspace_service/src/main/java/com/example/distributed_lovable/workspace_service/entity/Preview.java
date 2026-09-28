package com.example.distributed_lovable.workspace_service.entity;

import com.example.distributed_lovable.common_lib.enums.PreviewStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Preview {
    Long id;
    Project project;
    String namespace;
    String podName;
    String previewUrl;
    PreviewStatus status;

    Instant startedAt;
    Instant terminatedAt;
    Instant createdAt;
}
