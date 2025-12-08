package game.guide.ai.component.telegram;

import game.guide.ai.model.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserMessageKeeper {
    
    private static final int MAX_RECENT_MESSAGES = 20;
    private static final String PROCESSING_MESSAGE = "⏳ 正在處理您上一個問題，請稍等片刻...";
    
    private final Map<Long, List<UserMessage>> userRecentMessages = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> processingUsers = new ConcurrentHashMap<>();
    
    public void store(long userId, String text, String imageUrl, String userName, long chatId) {
        UserMessage userMessage = new UserMessage(
                text,
                imageUrl,
                java.time.LocalDateTime.now(),
                userName,
                chatId
        );

        List<UserMessage> messages = userRecentMessages.computeIfAbsent(userId, k -> new ArrayList<>());
        messages.add(userMessage);

        if (messages.size() > MAX_RECENT_MESSAGES) {
            messages.remove(0);
        }

        log.debug("暫存用戶 {} 的消息: {}", userId, text);
    }
    
    public List<UserMessage> getHistoricalMessages(long userId) {
        List<UserMessage> messages = userRecentMessages.getOrDefault(userId, new ArrayList<>());
        log.info("獲取用戶 {} 的 {} 則暫存歷史訊息", userId, messages.size());
        return new ArrayList<>(messages);
    }

    public String combineMessages(List<UserMessage> messages) {
        return messages.stream()
                .map(message -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(message.getTimestamp().toString()).append("] ");
                    sb.append(message.getUserName()).append(": ");

                    if (message.getText() != null) {
                        sb.append(message.getText());
                    }

                    if (message.getImageUrl() != null) {
                        sb.append(" [包含圖片]");
                    }

                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }
    
    public void clearRecentMessages(long userId) {
        userRecentMessages.remove(userId);
    }
    
    public boolean isUserProcessing(long userId) {
        return processingUsers.getOrDefault(userId, false);
    }
    
    public void setUserProcessing(long userId, boolean processing) {
        if (processing) {
            processingUsers.put(userId, true);
        } else {
            processingUsers.remove(userId);
        }
    }
    
    public String getProcessingMessage() {
        return PROCESSING_MESSAGE;
    }

    public Map<Long, List<UserMessage>> getUserRecentMessages() {
        return userRecentMessages;
    }

    public Map<Long, Boolean> getProcessingUsers() {
        return processingUsers;
    }
}