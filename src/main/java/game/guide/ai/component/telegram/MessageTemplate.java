package game.guide.ai.component.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@Slf4j
public class MessageTemplate {
    public static SendMessage buildSendMessage(long chatId, String text, Integer replyToMessageId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.enableHtml(true);
        message.disableWebPagePreview();
        message.setText(text);

        if (replyToMessageId != null) {
            log.debug("設定回覆到訊息 ID: {}", replyToMessageId);
            message.setReplyToMessageId(replyToMessageId);
        }
        return message;
    }

    public static EditMessageText buildEditMessage(long chatId, String text, Integer messageId) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.enableHtml(true);
        editMessage.setText(text);
        return editMessage;
    }
}
