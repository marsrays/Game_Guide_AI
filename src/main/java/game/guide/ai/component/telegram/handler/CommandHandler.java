package game.guide.ai.component.telegram.handler;

import game.guide.ai.component.telegram.MessageTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandHandler {
    /**
     * 取得此處理器負責的命令字串
     *
     * @return 命令字串，例如 "/start", "/help" 等
     */
    String getHandlerKey();

    /**
     * 處理 Telegram 命令訊息
     *
     * @param userId 使用者ID
     * @param message Telegram 訊息物件，包含完整的訊息內容
     * @param bot Telegram bot實例，用於發送回覆
     */
    void handleLongPolling(long userId, Message message, TelegramLongPollingBot bot);

    /**
     * 處理 Telegram 命令訊息
     *
     * @param userId 使用者ID
     * @param message Telegram 訊息物件，包含完整的訊息內容
     * @return 要回傳給 Telegram 的 BotApiMethod 物件
     */
    BotApiMethod<?> handleWebhook(long userId, Message message, TelegramWebhookBot webhookBot);

    /**
     * 傳送簡單的 Telegram 回覆訊息
     *
     * @param chatId 聊天室ID
     * @param text 要傳送的文字訊息（支援HTML格式）
     * @param replyToMessageId 要回覆的訊息ID，如果為null則不回覆特定訊息
     * @throws TelegramApiException 當發送訊息失敗時拋出異常
     */
    default SendMessage getSimpleResponse(long chatId, String text, Integer replyToMessageId) {
        return MessageTemplate.buildSendMessage(chatId, text, replyToMessageId);
    }
}