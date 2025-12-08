package game.guide.ai.config;

import game.guide.ai.service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook;

@Slf4j
@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.webhook-baseurl}")
    private String baseurl;
    @Value("${telegram.bot.secret-token}")
    private String secretToken;

    @Bean
    public ServerlessWebhook serverlessWebhook() {
        return new ServerlessWebhook();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService bot, ServerlessWebhook webhook) throws TelegramApiException {
        log.info("[{}] Register webhook", bot.getBotUsername());
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class, webhook);

        String webhookUrl = baseurl + "/webhook/";
        SetWebhook setWebhook = SetWebhook.builder()
                .url(webhookUrl)
                .dropPendingUpdates(true) // 可選：丟棄未處理的更新
                .secretToken(secretToken)
                .build();

        telegramBotsApi.registerBot(bot, setWebhook);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot, setWebhook);
        return botsApi;
    }
}