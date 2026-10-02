package com.example.distributed_lovable.common_lib.event;

import lombok.Builder;

@Builder
public record FileStoreResponseEvent (
        Long projectId,
        String sagaId,
        boolean success,
        String errorMessage
){


}
