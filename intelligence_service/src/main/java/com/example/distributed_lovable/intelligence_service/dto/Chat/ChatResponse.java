package com.example.distributed_lovable.intelligence_service.dto.Chat;


import com.example.distributed_lovable.common_lib.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(

        Long id,
        String content,
        MessageRole role,

        List<ChatEventResponse> events,

        Integer tokensUsed,

        Instant createdAt
) {
}
