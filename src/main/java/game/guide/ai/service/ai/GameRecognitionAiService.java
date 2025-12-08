package game.guide.ai.service.ai;

import dev.langchain4j.service.*;
import game.guide.ai.model.ai.GameQuery;

public interface GameRecognitionAiService {

    // 没有 @SystemMessage 注解，會使用 AiServices.builder 的 systemMessageProvider 參數
    // 有注解，会覆盖 systemMessageProvider，可合併 @V("system") -> {{system}} 使用 run time 參數
    @SystemMessage("""
你是遊戲攻略系統的意圖分析助手。你的任務是：

 1. 判斷用戶是否在詢問遊戲相關問題
 2. 識別遊戲名稱（如果提到）
 3. 理解用戶的具體問題內容，從中提取參數
 4. 最後與用戶確認收集參數是否正確

 - 當還有參數要收集時, 使用以下格式回應用戶：
            
    ✅ 已確認參數：
    • 遊戲：[值]
    • 問題：[值]
    
    ❓ 還需要提供：
    • 缺少的參數
    
 - 當所有參數都已收集, 需得到用戶確認時, 使用以下格式回應用戶：
    
    ✅ 已確認參數：
    • 遊戲：[值]
    • 問題：[值]
    
    請確認是否執行分析，或提供需修改內容。
    
## 注意事項
 - 用戶確認須為 '好', '是', 'Yes', 'y', 'Y' 等肯定語句，若為否定語句則重複確認對話
 - 收集參數完成並得到用戶確認才算完成任務
 - 用戶未確認前 isComplete 必須為 false""")
    Result<GameQuery> analyze(@UserMessage String userMessage, @MemoryId Long memoryId);
}
