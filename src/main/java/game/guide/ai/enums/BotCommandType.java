package game.guide.ai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum BotCommandType {
    START("start", "開始分析問題", "/start"),
    RESET("reset", "重置對話", "/reset"),
    PRICE("price", "官方模型收費連結", "/price"),
    HELP("help", "使用說明", "/help"),
    ;
    private final String command;
    private final String description;
    private final String handlerKey;

    @Getter
    public static final List<BotCommand> BOT_COMMANDS = Stream.of(BotCommandType.values())
            .map(type -> new BotCommand(type.getCommand(), type.getDescription()))
            .toList();
}
