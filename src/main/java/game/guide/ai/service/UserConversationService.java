package game.guide.ai.service;

import game.guide.ai.enums.ConversationMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class UserConversationService {

    private static final int SESSION_TIMEOUT_MINUTES = 60; // 1小時超時
    private final Map<Long, ConversationMode> conversationMode = new ConcurrentHashMap<>(); // 用戶對話模式
    private final Map<Long, LocalDateTime> lastActivity = new ConcurrentHashMap<>(); // 最後活動時間

    public void startRequirementConfirmation(Long userId) {
        checkAndResetIfTimeout(userId);

        conversationMode.put(userId, ConversationMode.REQUIREMENT_CONFIRMATION);
        lastActivity.put(userId, LocalDateTime.now());

        log.info("開始需求確認模式 for user: {}", userId);
    }

    public void startGameGuide(Long userId) {
        checkAndResetIfTimeout(userId);

        conversationMode.put(userId, ConversationMode.GAME_GUIDE);
        lastActivity.put(userId, LocalDateTime.now());
        
        log.info("開始AI回應模式 for user: {}", userId);
    }

    public void clearSession(Long userId) {
        conversationMode.remove(userId);
        lastActivity.remove(userId);
        log.info("清除會話 for user: {}", userId);
    }

    private void resetSession(Long userId) {
        conversationMode.remove(userId);
        lastActivity.remove(userId);
        log.info("重置會話 for user: {}", userId);
    }

    private void checkAndResetIfTimeout(Long userId) {
        LocalDateTime lastTime = lastActivity.get(userId);
        if (lastTime != null && lastTime.isBefore(LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES))) {
            resetSession(userId);
        }
    }

    public void updateActivity(Long userId) {
        lastActivity.put(userId, LocalDateTime.now());
    }

    public ConversationMode getCurrentMode(Long userId) {
        checkAndResetIfTimeout(userId);
        return conversationMode.get(userId);
    }
}