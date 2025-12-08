package game.guide.ai.component.telegram;

import game.guide.ai.component.telegram.handler.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommandProcessor {
    
    private final Map<String, CommandHandler> commandHandlers;

    public CommandProcessor(List<CommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers.stream()
                .collect(Collectors.toMap(CommandHandler::getHandlerKey, Function.identity()));
    }

    public BotApiMethod<?> run(Message message, long userId, Boolean isLongPolling, TelegramLongPollingBot longPollingBot, TelegramWebhookBot webhookBot)  {
        String commandText = message.getText();
        // 去掉＠後面的字串
        if (commandText.contains("@")) {
            commandText = commandText.substring(0, commandText.indexOf("@"));
        }

        CommandHandler handler = commandHandlers.get(commandText);
        if (handler != null) {
            if (isLongPolling) {
                handler.handleLongPolling(userId, message, longPollingBot);
            } else {
                return handler.handleWebhook(userId, message, webhookBot);
            }
        } else {
            log.warn("No handler found for command: {}", commandText);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setReplyToMessageId(message.getMessageId());
            sendMessage.setChatId(message.getChatId());
            sendMessage.setText("未知的命令：" + commandText);
            if (isLongPolling) {
                try {
                    longPollingBot.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error("Failed to send unknown command message", e);
                }
            } else {
                return sendMessage;
            }
        }
        return null;
    }
}