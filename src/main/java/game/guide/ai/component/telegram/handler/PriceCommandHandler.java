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
public class PriceCommandHandler implements CommandHandler {
    private String responseText = """
💰 <b>AI 服務價格資訊</b>
🔗 <a href="https://openai.com/api/pricing/">OpenAI</a>
🔗 <a href="https://www.anthropic.com/pricing#api">Claude</a>
🔗 <a href="https://ai.google.dev/gemini-api/docs/pricing">Gemini</a>""";

    @Override
    public String getHandlerKey() {
        return BotCommandType.PRICE.getHandlerKey();
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