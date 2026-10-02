package com.example.distributed_lovable.intelligence_service.mapper;


import com.example.distributed_lovable.intelligence_service.dto.Chat.ChatResponse;
import com.example.distributed_lovable.intelligence_service.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    List<ChatResponse> fromListOfChatMesssage(List<ChatMessage> chatMessageList);
}
