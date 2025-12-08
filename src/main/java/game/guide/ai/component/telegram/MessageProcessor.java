package game.guide.ai.component.telegram;

import dev.langchain4j.service.Result;
import game.guide.ai.enums.ConversationMode;
import game.guide.ai.model.ai.AIRequest;
import game.guide.ai.model.ai.GameQuery;
import game.guide.ai.service.UserConversationService;
import game.guide.ai.service.ai.GameGuideAiService;
import game.guide.ai.service.ai.GameRecognitionAiService;
import game.guide.ai.util.MetadataHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MessageProcessor {
    private static final int MAX_MESSAGE_LENGTH = 4000;

    private final UserMessageKeeper messageKeeper;
    private final LoadingAnimation loadingAnimation;
    private final GameRecognitionAiService gameRecognitionAiService;
    private final GameGuideAiService gameGuideAiService;
    private final UserConversationService userConversationService;

    public MessageProcessor(UserMessageKeeper messageKeeper,
                            LoadingAnimation loadingAnimation,
                            GameRecognitionAiService gameguideAiService,
                            GameGuideAiService gameGuideAiService,
                            UserConversationService userConversationService) {
        this.messageKeeper = messageKeeper;
        this.loadingAnimation = loadingAnimation;
        this.gameRecognitionAiService = gameguideAiService;
        this.gameGuideAiService = gameGuideAiService;
        this.userConversationService = userConversationService;
    }

    public BotApiMethod<?> run(Message message, TelegramWebhookBot bot) {
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();
        String messageText = message.getText();
        Integer messageId = message.getMessageId();
        ConversationMode mode = userConversationService.getCurrentMode(userId);
        if (null == mode) {
            messageKeeper.store(userId, messageText, null, message.getFrom().getFirstName(), chatId);
            return null;  // 無模式先存訊息後用
        } else {
            userConversationService.updateActivity(userId);
            if (messageKeeper.isUserProcessing(userId)) {
                return MessageTemplate.buildSendMessage(chatId, messageKeeper.getProcessingMessage(), messageId);
            } else {
                messageKeeper.setUserProcessing(userId, true);
            }
            return switch (mode) {
                case REQUIREMENT_CONFIRMATION ->
                        processRequirementConfirmation(messageText, userId, chatId, messageId, bot);
                case GAME_GUIDE -> processGameGuide(messageText, userId, chatId, messageId, bot);
            };
        }
    }

    public BotApiMethod<?> processRequirementConfirmation(String messageText, long userId, long chatId, Integer messageId, TelegramWebhookBot bot) {
        Integer animaMsgId = sendResponse(chatId, "⏳ 參數確認中...", messageId, bot);

        try {
            // 問 AI 要花時間，這時候 loadingAnimation 就會起作用
            Result<GameQuery> analyzeResult = gameRecognitionAiService.analyze(messageText, userId);
            GameQuery gameQuery = analyzeResult.content();
            log.info("analysisParameters: {}", gameQuery);

            if (Boolean.TRUE.equals(gameQuery.getIsComplete())) {
                if (animaMsgId != null) {
                    loadingAnimation.startLoadingAnimation(chatId, animaMsgId, userId, bot);
                }

                userConversationService.startRequirementConfirmation(userId);
                userConversationService.startGameGuide(userId);

                // 解析完成直接串第二 AI
                AIRequest analysisRequest = convertToAIRequest(gameQuery);
                String response = gameGuideAiService.guide(analysisRequest, userId);

                sendFinalAnswer(chatId, response, messageId, animaMsgId, bot);
            } else {
                editMessage(chatId, animaMsgId, analyzeResult.content().getReplyMessage(), bot);
            }
        } catch (Exception e) {
            log.error("需求確認AI回應失敗", e);
            if (animaMsgId != null) {
                loadingAnimation.stopLoadingAnimation(chatId, animaMsgId, "❌ AI 回應暫時無法使用，請稍後再試。", bot);
            } else {
                return MessageTemplate.buildSendMessage(chatId, "❌ AI 回應暫時無法使用，請稍後再試。", messageId);
            }
        } finally {
            messageKeeper.setUserProcessing(userId, false);
        }
        return null;
    }

    private BotApiMethod<?> processGameGuide(String messageText, long userId, long chatId, Integer messageId, TelegramWebhookBot bot) {
        Integer animaMsgId = sendResponse(chatId, "⏳正在處理中", messageId, bot);

        try {
            if (animaMsgId != null) {
                loadingAnimation.startLoadingAnimation(chatId, animaMsgId, userId, bot);
            }
            String response = gameGuideAiService.guide(messageText, userId);
            sendFinalAnswer(chatId, response, messageId, animaMsgId, bot);
        } catch (Exception e) {
            log.error("分析AI回應失敗", e);
            if (animaMsgId != null) {
                loadingAnimation.stopLoadingAnimation(chatId, animaMsgId, "❌ AI 回應暫時無法使用，請稍後再試。", bot);
            } else {
                return MessageTemplate.buildSendMessage(chatId, "❌ AI 回應暫時無法使用，請稍後再試。", messageId);
            }
        } finally {
            messageKeeper.setUserProcessing(userId, false);
        }
        return null;
    }

    private AIRequest convertToAIRequest(GameQuery requirement) {
        AIRequest request = new AIRequest();
        request.setGame(requirement.getGame());
        request.setQuestionSummary(requirement.getQuestionSummary());
        return request;
    }

    public Integer sendResponse(long chatId, String text, Integer replyToMessageId, TelegramWebhookBot bot) {
        try {
            Message sentMessage = bot.execute(MessageTemplate.buildSendMessage(chatId, text, replyToMessageId));
            return sentMessage.getMessageId();
        } catch (TelegramApiException e) {
            log.error("發送訊息失敗", e);
            return null;
        }
    }

    public boolean editMessage(long chatId, Integer messageId, String text, TelegramWebhookBot bot) {
        try {
            bot.execute(MessageTemplate.buildEditMessage(chatId, text, messageId));
            return true;
        } catch (TelegramApiException e) {
            log.error("更新消息失敗", e);
            return false;
        }
    }

    private List<String> splitMessage(String message, int maxLength) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < message.length()) {
            int end = Math.min(start + maxLength, message.length());
            // 確保分段盡量在換行符號處切分，提升訊息可讀性。
            int newlineIdx = message.lastIndexOf('\n', end - 1);
            if (newlineIdx >= start) {
                end = newlineIdx + 1;
            }
            chunks.add(message.substring(start, end));
            start = end;
        }
        return chunks;
    }

    private void sendFinalAnswer(long chatId, String response, Integer messageId, Integer animaMsgId, TelegramWebhookBot bot) {
        if (response.length() <= MAX_MESSAGE_LENGTH) {
            response = response + "\n" + MetadataHolder.formatMetadata();
            loadingAnimation.stopLoadingAnimation(chatId, animaMsgId, response, bot);
        } else {
            List<String> chunks = splitMessage(response, MAX_MESSAGE_LENGTH);
            loadingAnimation.stopLoadingAnimation(chatId, animaMsgId, chunks.getFirst(), bot);
            for (int i = 1; i < chunks.size(); i++) {
                String text = chunks.get(i);
                if (i == chunks.size() - 1) {
                    text = text + "\n" + MetadataHolder.formatMetadata();
                }
                sendResponse(chatId, text, messageId, bot);
            }
        }
    }
}