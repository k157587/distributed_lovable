package com.example.distributed_lovable.intelligence_service.service;

import com.example.distributed_lovable.intelligence_service.dto.Chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AiGenerationService {
    Flux<StreamResponse> streamResponse(String message, Long projectId);
}
