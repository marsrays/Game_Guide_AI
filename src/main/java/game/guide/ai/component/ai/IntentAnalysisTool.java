package game.guide.ai.component.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import game.guide.ai.enums.Game;
import game.guide.ai.model.ai.GameQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntentAnalysisTool {

    @Tool("確認用戶的遊戲查詢資訊")
    public String confirmGameQuery(
            @P("遊戲") Game game,
            @P("問題內容") String questionContent) {

        log.info("確認遊戲查詢 - 遊戲: {}, 問題: {}", game, questionContent);

        // 構建查詢物件並存入 Context
        GameQuery query = GameQuery.builder()
                .game(game)
                .replyMessage(questionContent)
                .isComplete(true)
                .build();

        GameQueryContext.set(query);

        return String.format(
                "✓ 已確認：遊戲《%s》，問題「%s」。正在為你查詢攻略...",
                game.name(), questionContent
        );
    }

    /**
     * 遊戲查詢 Context（用於在兩個 AI 之間傳遞資訊）
     */
    class GameQueryContext {
        private static final ThreadLocal<GameQuery> context = new ThreadLocal<>();

        public static void set(GameQuery query) {
            context.set(query);
        }

        public static GameQuery get() {
            return context.get();
        }

        public static void clear() {
            context.remove();
        }
    }
}

