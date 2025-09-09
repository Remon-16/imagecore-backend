package com.tech.imagecorebackenduserservice.infrastructure.ai.service;

import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface ChatDomainService {

    String createChatId();

    String doChatService(ChatRequest chatRequest);

    Flux<ServerSentEvent<String>> doChatStreamService(ChatRequest chatRequest);
}
