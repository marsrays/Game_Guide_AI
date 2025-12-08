package game.guide.ai.component.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class LoadingAnimation {

    private final Map<Integer, CompletableFuture<Void>> loadingAnimations = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicBoolean> animationInterrupts = new ConcurrentHashMap<>();
    private final Map<Integer, Long> animationUserIds = new ConcurrentHashMap<>();
    private static final long ANIMATION_TIMEOUT_MS = 180_000; // 3分鐘

    public void startLoadingAnimation(long chatId, Integer messageId, long userId, TelegramWebhookBot bot) {
        if (messageId == null) return;

        AtomicBoolean interrupt = new AtomicBoolean(false);
        animationInterrupts.put(messageId, interrupt);
        animationUserIds.put(messageId, userId);

        CompletableFuture<Void> animation = CompletableFuture.runAsync(() -> {
            try {
                runAnimationLoop(chatId, messageId, interrupt, bot);
            } catch (Exception e) {
                log.error("動畫處理異常", e);
            } finally {
                cleanupAnimation(messageId);
            }
        });

        loadingAnimations.put(messageId, animation);
    }

    private void runAnimationLoop(long chatId, Integer messageId, AtomicBoolean interrupt, TelegramWebhookBot bot) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        int dotCount = 1;

        while (!Thread.currentThread().isInterrupted() && !interrupt.get() && (System.currentTimeMillis() - startTime) < ANIMATION_TIMEOUT_MS) {
            try {
                //noinspection BusyWait
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            // 檢查中斷狀態
            if (Thread.currentThread().isInterrupted() || interrupt.get()) {
                return;
            }

            String[] symbols = {"⏳", "⌛", "🔄"};
            String symbol = symbols[(dotCount - 1) % symbols.length];
            String dots = ".".repeat(dotCount);

            // 更新訊息前最後一次檢查中斷狀態
            if (Thread.currentThread().isInterrupted() || interrupt.get()) {
                return;
            }

            if (!updateAnimationMessage(chatId, messageId, symbol + " 正在處理中" + dots, bot)) {
                return;
            }

            dotCount = (dotCount % 3) + 1;
        }

        if (!Thread.currentThread().isInterrupted() && !interrupt.get()) {
            updateAnimationMessage(chatId, messageId, "⏰ 處理時間過長，請稍後再試", bot);
        }
    }

    private boolean updateAnimationMessage(long chatId, Integer messageId, String text, TelegramWebhookBot bot) {
        try {
            bot.execute(MessageTemplate.buildEditMessage(chatId, text, messageId));
            return true;
        } catch (TelegramApiException e) {
            log.error("更新動畫消息失敗", e);
            return false;
        }
    }

    private void cleanupAnimation(Integer messageId) {
        animationInterrupts.remove(messageId);
        animationUserIds.remove(messageId);
        loadingAnimations.remove(messageId);
    }

    public void stopLoadingAnimation(long chatId, Integer messageId, String finalMessage, TelegramWebhookBot bot) {
        if (messageId == null) return;

        // 首先設置中斷標記
        AtomicBoolean interrupt = animationInterrupts.get(messageId);
        if (interrupt != null) {
            interrupt.set(true);
        }

        // 取消並等待動畫任務完成
        CompletableFuture<Void> animation = loadingAnimations.remove(messageId);
        if (animation != null) {
            animation.cancel(true);
            try {
                // 等待動畫任務真正完成，最多等待2秒
                animation.get(2, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                // 超時或被中斷都忽略，繼續執行
            }
        }

        // 保存 userId 在清理之前 (暫時不使用，但保留以便未來使用)
        // Long userId = animationUserIds.get(messageId);
        
        // 立即清理資源
        cleanupAnimation(messageId);

        // 額外等待一點時間確保沒有延遲的動畫更新
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 最後更新最終消息
        if (finalMessage != null) {
            updateAnimationMessage(chatId, messageId, finalMessage, bot);
        }
    }

    public void clearAnimationsForUser(long userId) {
        // 找到屬於指定用戶的所有動畫消息ID
        animationUserIds.entrySet().stream()
                .filter(entry -> entry.getValue().equals(userId))
                .map(Map.Entry::getKey)
                .forEach(messageId -> {
                    // 中斷對應的動畫
                    AtomicBoolean interrupt = animationInterrupts.get(messageId);
                    if (interrupt != null) {
                        interrupt.set(true);
                    }

                    // 取消對應的動畫任務
                    CompletableFuture<Void> animation = loadingAnimations.get(messageId);
                    if (animation != null) {
                        animation.cancel(true);
                    }

                    // 清理資源
                    cleanupAnimation(messageId);
                });
    }
}