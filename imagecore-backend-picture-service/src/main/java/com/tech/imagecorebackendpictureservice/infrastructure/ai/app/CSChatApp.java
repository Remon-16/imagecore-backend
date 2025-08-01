package com.tech.imagecorebackendpictureservice.infrastructure.ai.app;

import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class CSChatApp {


    private final ChatClient csChatClient;

    @Resource
    private Advisor ImageCoreRagAdvisor;

    private String CS_SYSTEM_PROMPT = "你是一个客服，请根据提供的问题和答案进行回复，如果用户的问题没有命中，则直接回复“您好，我不清楚这个问题，请通过页面下方联系方式，联系网站管理员。”。千万不要回答没有记录的问题。";

    public CSChatApp(ChatModel dashscopeChatModel, JdbcTemplate jdbcTemplate) {
        // 构造 ChatMemoryRepository 和 ChatMemory
        ChatMemoryRepository chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository).maxMessages(20)
                .build();
        csChatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(CS_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    public String getChat(String userMessage, String chatId){
        ChatResponse chatResponse = csChatClient
                .prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(ImageCoreRagAdvisor)
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    public Flux<String> doChatStream(String chatId, String userMessage){
        return csChatClient
                .prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(ImageCoreRagAdvisor)
                .stream()
                .content();
    }


}
