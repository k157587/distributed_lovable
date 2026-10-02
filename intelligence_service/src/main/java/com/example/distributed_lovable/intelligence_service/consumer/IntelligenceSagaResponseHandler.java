package com.example.distributed_lovable.intelligence_service.consumer;

import com.example.distributed_lovable.common_lib.enums.ChatEventStatus;
import com.example.distributed_lovable.common_lib.event.FileStoreResponseEvent;
import com.example.distributed_lovable.intelligence_service.repository.ChatEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IntelligenceSagaResponseHandler {

    private final ChatEventRepository chatEventRepository;

    @Transactional
    @KafkaListener(topics = "file-store-responses",groupId = "intelligence-group")
    public void handleSagaResponses(FileStoreResponseEvent responseEvent){

        chatEventRepository.findBySagaId(responseEvent.sagaId()).ifPresent(chatEvent -> {

            if (!ChatEventStatus.PENDING.equals(chatEvent.getStatus())){
                log.info("Response for saga {} already handled.skipping ", responseEvent.sagaId());
                return;
            }

            if (responseEvent.success()){
                chatEvent.setStatus(ChatEventStatus.CONFIRMED);
                log.info("saga {} confirmed ", responseEvent.sagaId());

            }
            else{
                log.warn("Saga {} failed. deleting event.", responseEvent.sagaId());
                chatEvent.setStatus(ChatEventStatus.FAILED);
            }
        });
    }
}
