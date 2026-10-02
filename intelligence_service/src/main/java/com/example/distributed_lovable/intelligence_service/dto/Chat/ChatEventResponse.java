package com.example.distributed_lovable.intelligence_service.dto.Chat;

import com.example.distributed_lovable.common_lib.enums.ChatEventType;

public record ChatEventResponse(


        Long id,
        ChatEventType type,
        Integer sequenceOrder,
        String content,
        String filePath,
        String metadata
) {
}
