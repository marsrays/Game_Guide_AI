package game.guide.ai.service;

import game.guide.ai.enums.BotCommandType;
import game.guide.ai.component.telegram.CommandProcessor;
import game.guide.ai.component.telegram.MessageProcessor;
import game.guide.ai.util.MetadataHolder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Service
public class TelegramBotService extends TelegramWebhookBot {    // TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;
    @Value("${telegram.bot.token}")
    private String botToken;

    private final CommandProcessor commandProcessor;
    private final MessageProcessor messageProcessor;

    private final long startupTime = System.currentTimeMillis() / 1000;

    public TelegramBotService(CommandProcessor commandProcessor,
                              MessageProcessor messageProcessor) {
        this.commandProcessor = commandProcessor;
        this.messageProcessor = messageProcessor;
    }
    
    @PostConstruct
    public void init() {
        log.info("初始化 Telegram Bot: username={}", botUsername);
        registerCommands();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return "/" + getBotUsername();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            // Webhook 可以在設置 dropPendingUpdates 忽略伺服器啟動前的舊訊息
            // Webhook 呼叫失敗會採 [指數退避策略] 等待時間隨失敗次數加倍（如1秒後變為2秒，再變為4秒等）再加入“抖動”（隨機延遲）

            long chatId = message.getChatId();
            long userId = message.getFrom().getId();
            String userName = message.getFrom().getFirstName();
            log.info("收到來自用戶 {}({}) 的訊息，聊天室: {}", userId, userName, chatId);
            if (message.hasText()) {
                String messageText = message.getText();
                log.info("訊息內容: {}", messageText);
                if (message.isCommand()) {
                    return commandProcessor.run(message, userId, false, null, this);
                } else {
                    return messageProcessor.run(message, this);
                }
            }
        }
        return null;
    }

    // 使用 TelegramLongPollingBot 要實作 @AILogAnalysis_bot
//    @Override
    public void onUpdateReceived(Update update) {
        Thread.ofVirtual().start(() -> processUpdate(update));
    }

    /**
     * 註冊機器人指令，後續 message.isCommand() 可以辨識指令
     */
    private void registerCommands() {
        List<BotCommand> commands = BotCommandType.getBOT_COMMANDS();
        if (commands.isEmpty()) {
            DeleteMyCommands deleteMyCommands = new DeleteMyCommands();
            try {
                execute(deleteMyCommands);
            } catch (TelegramApiException e) {
                log.error("[{}] Delete commands failed.", getBotUsername(), e);
            }
            return;
        }

        log.info("[{}] Register commands", getBotUsername());
        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);
        try {
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.error("[{}] Register commands failed.", getBotUsername(), e);
        }
    }

    private void processUpdate(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Message message = update.getMessage();

                // Long Polling 沒有直接跳過舊訊息的控制，只能自己設置忽略伺服器啟動前的舊訊息
                if (message.getDate() < startupTime) {
                    log.info("忽略舊訊息: messageId={}, date={}", message.getMessageId(), message.getDate());
                    return;
                }

                long chatId = message.getChatId();
                long userId = message.getFrom().getId();
                String userName = message.getFrom().getFirstName();
                log.info("收到來自用戶 {}({}) 的訊息，聊天室: {}", userId, userName, chatId);
                if (message.hasText()) {
                    String messageText = message.getText();
                    log.info("訊息內容: {}", messageText);
                    if (message.isCommand()) {
//                        commandProcessor.run(message, userId, true, this);
                    } else {
//                        messageProcessor.run(message, this);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[{}] processUpdate failed.", getBotUsername(), e);
        } finally {
            MetadataHolder.clear();
        }
    }
}