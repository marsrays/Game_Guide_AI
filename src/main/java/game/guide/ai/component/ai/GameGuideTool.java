package game.guide.ai.component.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import game.guide.ai.enums.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Slf4j
@Component
public class GameGuideTool {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public GameGuideTool(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    @Tool("搜索遊戲攻略知識庫，返回相關的攻略內容")
    public String searchGameGuide(@P("搜索查詢內容，使用核心關鍵詞") Game game,
            @P("搜索查詢內容，使用核心關鍵詞") String query) {
        log.info("搜索攻略 - 遊戲: {} ({}), 查詢: {}",
                game.getChineseName(), game.name(), query);

        // 向量化查詢
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 構建搜索請求（使用 Game Enum 作為過濾條件）
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(6)
                .minScore(0.6)
                .filter(metadataKey("game").isEqualTo(game.name())) // 使用 Enum name
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        if (result.matches().isEmpty()) {
            String notFoundMsg = String.format(
                    "抱歉，在《%s》的知識庫中未找到關於「%s」的相關攻略。\n" +
                            "請嘗試換個說法，或者提供更多細節。",
                    game.getChineseName(), query
            );
            log.info("未找到攻略資料");
            return notFoundMsg;
        }

        // 格式化結果
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("找到 %d 條《%s》的相關攻略：\n\n",
                result.matches().size(), game.getChineseName()));

        for (int i = 0; i < result.matches().size(); i++) {
            EmbeddingMatch<TextSegment> match = result.matches().get(i);
            TextSegment segment = match.embedded();
            sb.append(String.format("【攻略 %d】(相似度: %.2f)\n", i + 1, match.score()));

            // 添加元數據資訊
//            Metadata metadata = segment.metadata();
//            String chapter = metadata.getString("chapter");
//            String category = metadata.getString("category");
//            String source = metadata.getString("source");
//
//            if (chapter != null) {
//                sb.append(String.format("章節：%s\n", chapter));
//            }
//            if (category != null) {
//                sb.append(String.format("分類：%s\n", category));
//            }
//            if (source != null) {
//                sb.append(String.format("來源：%s\n", source));
//            }

            sb.append("\n內容：\n");
            sb.append(segment.text());

            if (i < result.matches().size() - 1) {
                sb.append("---\n\n");
            }
        }

        log.info("✓ 找到 {} 條攻略資料", result.matches().size());

        return sb.toString();
    }
}