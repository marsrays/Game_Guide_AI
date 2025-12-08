package game.guide.ai.component.telegram.handler;

import game.guide.ai.enums.BotCommandType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class HelpCommandHandler implements CommandHandler {
    private String responseText = """
🤖 <b>AI 指令功能列表</b>

💬 /price - 查看價格、使用量及套餐資訊
🗣️ /start - 開始AI對話並進行需求確認
🗑️ /reset - 立即停止AI對話並清除所有歷史訊息及AI記憶
❓ /help  - 顯示此幫助訊息""";
    
    @Override
    public String getHandlerKey() {
        return BotCommandType.HELP.getHandlerKey();
    }
    
    @Override
    public void handleLongPolling(long userId, Message message, TelegramLongPollingBot bot) {
        try {
            bot.execute(getSimpleResponse(message.getChatId(), responseText, message.getMessageId()));
        } catch (TelegramApiException e) {
            log.error("發送訊息失敗", e);
        }
    }

    @Override
    public BotApiMethod<?> handleWebhook(long userId, Message message, TelegramWebhookBot webhookBot) {
        return getSimpleResponse(message.getChatId(), responseText, message.getMessageId());
    }
}