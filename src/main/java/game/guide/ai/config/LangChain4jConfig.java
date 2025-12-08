package game.guide.ai.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import game.guide.ai.component.ai.GameGuideTool;
import game.guide.ai.service.ai.GameGuideAiService;
import game.guide.ai.service.ai.GameRecognitionAiService;
import game.guide.ai.util.MetadataHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class LangChain4jConfig {

    // 可管理的記憶提供器類
    public static class ManagedChatMemoryProvider implements ChatMemoryProvider {
        private final Map<Object, ChatMemory> memories = new ConcurrentHashMap<>();
        private final int maxMessages;

        public ManagedChatMemoryProvider(int maxMessages) {
            this.maxMessages = maxMessages;
        }

        @Override
        public ChatMemory get(Object memoryId) {
            return memories.computeIfAbsent(memoryId,
                    id -> MessageWindowChatMemory.withMaxMessages(maxMessages));
        }

        public void clearMemory(Object memoryId) {
            ChatMemory memory = memories.get(memoryId);
            if (memory != null) {
                memory.clear();
            }
        }

        public void removeMemory(Object memoryId) {
            memories.remove(memoryId);
        }

        public boolean hasMemory(Object memoryId) {
            return memories.containsKey(memoryId);
        }
    }

    @Value("${langchain4j.api-key}")
    private String apiKey;
    @Value("${langchain4j.category-model-name:gpt-4}")
    private String categoryModelName;
    @Value("${langchain4j.analytics-model-name:gpt-4}")
    private String analyticsModelName;

    @Bean
    public ChatModel categoryModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(categoryModelName)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(60))
                .responseFormat(ResponseFormatType.JSON.name())
//                .logRequests(true)
                .listeners(List.of(new ChatModelListener() {
                    @Override
                    public void onRequest(ChatModelRequestContext requestContext) {
                        List<ChatMessage> req = requestContext.chatRequest().messages();
                        log.info("Requesting chat model: {}, messages: {}", categoryModelName, req);
                        ChatModelListener.super.onRequest(requestContext);
                    }
                }))
                .build();
    }

    @Bean
    public ChatModel answerModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(analyticsModelName)
                .temperature(0.7) // 較高溫度用於回應生成
                .timeout(Duration.ofSeconds(180))
//                .logRequests(true)
//                .logResponses(true)
                .listeners(List.of(new ChatModelListener() {
                    @Override
                    public void onResponse(ChatModelResponseContext responseContext) {
                        ModelProvider modelProvider = responseContext.modelProvider();
                        var chatResponse = responseContext.chatResponse();
                        if (chatResponse != null && chatResponse.metadata() != null) {
                            var metadata = chatResponse.metadata();
                            var responseMetadata = MetadataHolder.ResponseMetadata.builder()
                                    .tokenUsage(metadata.tokenUsage())
                                    .modelName(metadata.modelName())
                                    .modelProvider(modelProvider)
                                    .build();
                            // 將metadata存儲到ThreadLocal供後續使用
                            MetadataHolder.setMetadata(responseMetadata);
                        }
                        ChatModelListener.super.onResponse(responseContext);
                    }
                }))
                .build();
    }

    @Bean
    public GameRecognitionAiService recognitionAgent(ChatModel categoryModel, ManagedChatMemoryProvider recognitionChatMemoryProvider) {
        return AiServices.builder(GameRecognitionAiService.class)
                .chatModel(categoryModel)
                .chatMemoryProvider(recognitionChatMemoryProvider)
                .build();
    }

    @Bean
    public GameGuideAiService guideAgent(ChatModel answerModel, GameGuideTool gameGuideTool, ManagedChatMemoryProvider guideChatMemoryProvider) {
        return AiServices.builder(GameGuideAiService.class)
                .chatModel(answerModel)
                .chatMemoryProvider(guideChatMemoryProvider)
                .tools(gameGuideTool)
                .build();
    }

    @Bean
    public ManagedChatMemoryProvider recognitionChatMemoryProvider() {
        return new ManagedChatMemoryProvider(20);
    }

    @Bean
    public ManagedChatMemoryProvider guideChatMemoryProvider() {
        return new ManagedChatMemoryProvider(40);
    }
}