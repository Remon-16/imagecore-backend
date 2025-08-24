package com.tech.imagecorebackenduserservice.infrastructure.ai.app;

import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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
public class DrawChatApp {

    private final ChatClient drawChatClient;

    private final String DRAW_SYSTEM_PROMPT = "你是一个资深的绘图助理，能够根据用户的想法，生成更加优化的绘图大模型提示词，使其能够更加贴合用户的想法。需要注意的是，不同用户的绘画经验和使用大模型进行绘图的经验可能有较大差异。所以你应该把这个事情进行分解。首先通过用户给出的信息判断用户的在绘画和使用大模型经验上的专业程度以及用户的需求是否足够清晰；根据专业程度以及需求描述的清晰程度，判断是否需要用户补充信息；如果感觉用户的描述不够专业或者不够清晰，则向用户提问（比如可以问用户具体的偏好，画风的偏向，色彩的倾向，大致的用途以及是否需要英文提示词prompt）。如果说感觉用户提出的需求很清晰，专业性也比较好，则先优化提示词，再去问用户是否需要优化。另外需要注意的是，回复用户的话需要精简，不要超过800字。";

    public DrawChatApp(ChatModel dashscopeChatModel, JdbcTemplate jdbcTemplate) {
        // 构造 ChatMemoryRepository 和 ChatMemory
        ChatMemoryRepository chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository).maxMessages(20)
                .build();
        drawChatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(DRAW_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    public String getDrawChat(String userMessage, String chatId){
        ChatResponse chatResponse = drawChatClient
                .prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    public Flux<String> doChatStream(String chatId, String userMessage){
        return drawChatClient
                .prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}
