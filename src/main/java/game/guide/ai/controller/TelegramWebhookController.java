package game.guide.ai.controller;

import game.guide.ai.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class TelegramWebhookController {
    @Value("${telegram.bot.secret-token}")
    private String secretToken;

    private final ServerlessWebhook webhook;
    private final TelegramBotService bot;

    @PostMapping("/callback/{botName}")
    public ResponseEntity<BotApiMethod<?>> onUpdateReceived(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String token,
            @RequestBody Update update,
            @PathVariable String botName) throws TelegramApiException {
        // 驗證 token
        if (!secretToken.equals(token)) {
            // Token 不匹配,拒絕請求
            log.error("Telegram SecretToken: {} is wrong token, access denied !", token);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("Telegram webhook: {}", botName);
        return ResponseEntity.ok(webhook.updateReceived("/" + botName, update));  // 其實是要輸入 bot.getBotPath() 但實務上傳入的是 botName
    }

    /**
     * 獲取 Webhook 信息
     */
    @GetMapping("/info")
    public ResponseEntity<WebhookInfo> getWebhookInfo() throws TelegramApiException {
        return ResponseEntity.ok(bot.getWebhookInfo());
    }

    /**
     * 刪除 Webhook（切換回 polling 模式時使用）
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Boolean> deleteWebhook() throws TelegramApiException {
        DeleteWebhook deleteWebhook = DeleteWebhook.builder().dropPendingUpdates(true).build();
        return ResponseEntity.ok(bot.execute(deleteWebhook));
    }
}
