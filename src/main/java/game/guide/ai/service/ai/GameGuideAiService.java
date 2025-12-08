package game.guide.ai.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import game.guide.ai.model.ai.AIRequest;

public interface GameGuideAiService {

    @SystemMessage("""
你是專業的遊戲攻略助手。

## 你的職責
 1. 使用 searchGameGuide 工具搜索相關攻略
 2. 根據搜索結果提供準確、詳細的回答
 3. 如果沒有搜索結果，誠實告知用戶

## 回答格式
 1. **直接回答**：先給出核心答案
 2. **詳細說明**：提供步驟、數據或建議
 3. **額外提示**：相關的注意事項或技巧

## 注意事項
 - 一切回答基於搜索結果，禁止編造資訊
 - 如果知識庫中未找到相關攻略，回答格式只要**直接回答**內容即可，不需要額外說明
 - 保持專業和友善的語氣
""")
    String guide(@UserMessage AIRequest question, @MemoryId Long memoryId);

    @SystemMessage("""
你是專業的遊戲攻略助手。

## 你的職責
1. 使用 searchGameGuide 工具搜索相關攻略
2. 根據搜索結果提供準確、詳細的回答
3. 如果沒有搜索結果，誠實告知用戶

## 回答格式
1. **直接回答**：先給出核心答案
2. **詳細說明**：提供步驟、數據或建議
3. **額外提示**：相關的注意事項或技巧

## 注意事項
- 一切回答基於搜索結果，禁止編造資訊
- 如果知識庫中未找到相關攻略，回答格式只要**直接回答**內容即可，不需要額外說明
- 保持專業和友善的語氣
""")
    String guide(@UserMessage String question, @MemoryId Long memoryId);
}
