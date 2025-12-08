package game.guide.ai.model.ai;

import dev.langchain4j.model.output.structured.Description;
import game.guide.ai.enums.Game;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameQuery {
    @Description("遊戲名稱, 用戶輸入的不看大小寫, 只要符合就行, 沒有值要設為 null")
    private Game game;

    @Description("回應給用戶的訊息")
    private String replyMessage;

    @Description("用戶問題的簡潔摘要，用於後續分析階段")
    private String questionSummary;

    @Description("參數全部收集完成且用戶確認，true表示可以進入分析模式，false表示需要繼續收集")
    private Boolean isComplete;
}