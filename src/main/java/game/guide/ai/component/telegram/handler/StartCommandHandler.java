package game.guide.ai.component.telegram.handler;

import game.guide.ai.component.telegram.MessageProcessor;
import game.guide.ai.component.telegram.UserMessageKeeper;
import game.guide.ai.enums.BotCommandType;
import game.guide.ai.model.UserMessage;
import game.guide.ai.service.UserConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private final UserConversationService conversationService;
    private final UserMessageKeeper userMessageKeeper;
    private final MessageProcessor messageProcessor;

    @Override
    public String getHandlerKey() {
        return BotCommandType.START.getHandlerKey();
    }

    @Override
    public void handleLongPolling(long userId, Message message, TelegramLongPollingBot bot) {
        try {
            bot.execute(getSimpleResponse(message.getChatId(), "not implement", message.getMessageId()));
        } catch (TelegramApiException e) {
            log.error("發送訊息失敗", e);
        }
    }

    @Override
    public BotApiMethod<?> handleWebhook(long userId, Message message, TelegramWebhookBot webhookBot) {
        // 檢查是否正在處理該用戶的請求
        if (userMessageKeeper.isUserProcessing(userId)) {
            return getSimpleResponse(message.getChatId(), userMessageKeeper.getProcessingMessage(), message.getMessageId());
        } else {
            // 標記開始處理
            userMessageKeeper.setUserProcessing(userId, true);
        }

        // 開始需求確認模式
        conversationService.startRequirementConfirmation(userId);

        // 透過暫存獲取該用戶的歷史訊息
        List<UserMessage> historicalMessages = userMessageKeeper.getHistoricalMessages(userId);

        if (historicalMessages.isEmpty()) {
            userMessageKeeper.setUserProcessing(userId, false);
            return getSimpleResponse(message.getChatId(), "✅ 進入需求確認模式！\n\n請發送您的問題內容，我會協助您確認分析參數。\n\n💡 提示：\n- 使用 /reset 清除會話\n- 超過1小時無活動會自動重置", message.getMessageId());
        }

        // 組合歷史訊息內容進行需求確認分析
        String combinedMessages = userMessageKeeper.combineMessages(historicalMessages);
        return messageProcessor.processRequirementConfirmation(combinedMessages, userId, message.getChatId(), message.getMessageId(), webhookBot);
    }

}