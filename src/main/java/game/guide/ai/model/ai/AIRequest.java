package game.guide.ai.model.ai;

import dev.langchain4j.model.output.structured.Description;
import game.guide.ai.enums.Game;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    @Description("遊戲名稱, 用戶輸入的不看大小寫, 只要符合就行")
    private Game game;

    @Description("用戶問題的簡潔摘要，用於後續分析階段")
    private String questionSummary;
}