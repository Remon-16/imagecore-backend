package com.tech.imagecorebackenduserservice.infrastructure.ai.service;

import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import reactor.core.publisher.Flux;

public interface ChatDomainService {

    Flux<String> doChatStreamService(ChatRequest chatRequest);
}
