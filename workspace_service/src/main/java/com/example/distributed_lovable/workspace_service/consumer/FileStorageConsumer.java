package com.example.distributed_lovable.workspace_service.consumer;


import com.example.distributed_lovable.common_lib.event.FileStoreRequestEvent;
import com.example.distributed_lovable.common_lib.event.FileStoreResponseEvent;
import com.example.distributed_lovable.workspace_service.entity.ProcessedEvent;
import com.example.distributed_lovable.workspace_service.repository.ProcessedEventRepository;
import com.example.distributed_lovable.workspace_service.service.ProjectFileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageConsumer {

    private final ProjectFileService projectFileService;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    @Transactional
    @KafkaListener(topics = "file-storage-request-event",groupId = "workspace-group")
    public void consumeFileEvent(FileStoreRequestEvent requestEvent){

        if (processedEventRepository.existsById(requestEvent.sagaId())){
            log.error("dupicate saga Id:{}, resending previous ack",requestEvent.sagaId());
            sendResponse(requestEvent,true,null);
            return;
        }
        try{

        log.info("saving file : {}",requestEvent.filePath());
        projectFileService.saveFile(requestEvent.projectId(),requestEvent.filePath(),requestEvent.content());
        processedEventRepository.save(new ProcessedEvent(
                requestEvent.sagaId(), LocalDateTime.now()
        ));
        sendResponse(requestEvent,true,null);
        }
        catch (Exception e){
            log.error("error saving file: {}",requestEvent.filePath());
            sendResponse(requestEvent,false,e.getMessage());
        }
    }

    private void sendResponse(FileStoreRequestEvent req,Boolean success,String error){
        FileStoreResponseEvent responseEvent = FileStoreResponseEvent.builder()
                .sagaId(req.sagaId())
                .errorMessage(error)
                .success(success)
                .projectId(req.projectId()).build();

        kafkaTemplate.send("file-store-responses",responseEvent);
    }
}
