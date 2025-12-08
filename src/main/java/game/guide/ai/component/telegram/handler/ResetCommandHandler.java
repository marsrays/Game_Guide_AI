package game.guide.ai.component.telegram.handler;

import game.guide.ai.component.telegram.LoadingAnimation;
import game.guide.ai.component.telegram.UserMessageKeeper;
import game.guide.ai.config.LangChain4jConfig.ManagedChatMemoryProvider;
import game.guide.ai.enums.BotCommandType;
import game.guide.ai.service.UserConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class ResetCommandHandler implements CommandHandler {

    private final UserConversationService conversationService;
    private final LoadingAnimation loadingAnimation;
    private final UserMessageKeeper userMessageKeeper;
    private final ManagedChatMemoryProvider recognitionChatMemoryProvider;
    private final ManagedChatMemoryProvider guideChatMemoryProvider;


    public ResetCommandHandler(UserConversationService conversationService,
                                 LoadingAnimation loadingAnimation,
                               UserMessageKeeper userMessageKeeper,
                               ManagedChatMemoryProvider recognitionChatMemoryProvider,
                               ManagedChatMemoryProvider guideChatMemoryProvider) {
        this.conversationService = conversationService;
        this.loadingAnimation = loadingAnimation;
        this.userMessageKeeper = userMessageKeeper;
        this.recognitionChatMemoryProvider = recognitionChatMemoryProvider;
        this.guideChatMemoryProvider = guideChatMemoryProvider;

    }

    @Override
    public String getHandlerKey() {
        return BotCommandType.RESET.getHandlerKey();
    }

    @Override
    public void handleLongPolling(long userId, Message message, TelegramLongPollingBot bot) {

    }

    @Override
    public BotApiMethod<?> handleWebhook(long userId, Message message, TelegramWebhookBot webhookBot) {

        conversationService.clearSession(userId);
        userMessageKeeper.clearRecentMessages(userId);
        userMessageKeeper.setUserProcessing(userId, false);
        loadingAnimation.clearAnimationsForUser(userId);
        recognitionChatMemoryProvider.clearMemory(userId);
        guideChatMemoryProvider.clearMemory(userId);

        log.info("已清除用戶 {} 的所有 AI 記憶", userId);

        return getSimpleResponse(message.getChatId(), "🗑️已清除相關歷史訊息及AI記憶", message.getMessageId());
    }

}