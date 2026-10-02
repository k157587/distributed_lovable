package com.example.distributed_lovable.intelligence_service.llm.advisors;

import com.example.distributed_lovable.common_lib.dto.FileNode;
import com.example.distributed_lovable.intelligence_service.client.WorkspaceClient;
import com.example.distributed_lovable.intelligence_service.security.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileTreeContextAdvisor implements StreamAdvisor {

    private final WorkspaceClient workspaceClient;

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain streamAdvisorChain) {
        Map<String, Object> context = request.context();

        Long projectId = Long.parseLong(context.getOrDefault("projectId", 0).toString());
        Authentication auth = (Authentication) context.get("authentication");

        ChatClientRequest augmentedChatClientRequest = augmentRequestWithFileTree(request, projectId, auth);

        return streamAdvisorChain.nextStream(augmentedChatClientRequest);
    }

    private ChatClientRequest augmentRequestWithFileTree(ChatClientRequest request, Long projectId, Authentication auth) {

        List<Message> incomingMessage = request.prompt().getInstructions();

        Message systemMessage = incomingMessage.stream()
                .filter(m -> m.getMessageType() == MessageType.SYSTEM)
                .findFirst()
                .orElse(null);

        List<Message> userMessage = incomingMessage.stream()
                .filter(m -> m.getMessageType() != MessageType.SYSTEM)
                .toList();

        List<Message> allMessages = new ArrayList<>();

        if (systemMessage != null) {
            allMessages.add(systemMessage);
        }

        // Feign call now runs WITH the user's login attached
        List<FileNode> fileTree = AuthContext
                .callAs(auth, () -> workspaceClient.getFileTree(projectId))
                .files();

        String fileTreeContext = "\n\n ---- FILE_TREE ---\n" + fileTree.toString();

        allMessages.add(new SystemMessage(fileTreeContext));
        allMessages.addAll(userMessage);

        return request
                .mutate()
                .prompt(new Prompt(allMessages, request.prompt().getOptions()))
                .build();
    }

    @Override
    public String getName() {
        return "FileTreeContextAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}