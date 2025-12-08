package game.guide.ai.config;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

@Configuration
public class EmbeddingConfig {

    @Value("${langchain4j.api-key}")
    private String apiKey;

    @Bean
    public DocumentSplitter documentSplitter() {
        return DocumentSplitters.recursive(400, 60);
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        // 根據你使用的模型選擇，例如：
        return OpenAiEmbeddingModel.builder()
//                .baseUrl("https://apipro.ai/v1")  // 你的第三方 OpenAI 轉接
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            DocumentSplitter documentSplitter,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .textSegmentTransformer(this.createTextSegmentTransformer())
                .build();

        return  ingestor;
    }

    // 將轉換邏輯抽取為私有方法，提高可讀性
    private TextSegmentTransformer createTextSegmentTransformer() {
        return textSegment -> {
            String fileName = textSegment.metadata().getString("file_name");

            // 略過空行或只有單一特殊字元的行
            String text = textSegment.text().trim();
            if (text.isEmpty() || text.length() == 1) {
                return null;
            }

            // 移除所有空行
            text = text.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.joining("\n"));

            System.out.println("fileName=" + fileName +
                    ", index=" + textSegment.metadata().getString("index") +
                    ", text=" + text);

            // 根據檔名判斷遊戲
            String game = determineGame(fileName);

            // 將遊戲寫入 metadata
            Metadata metadata = textSegment.metadata().copy();
            metadata.put("game", game);

            return TextSegment.from(text, metadata);
        };
    }

    private String determineGame(String fileName) {
        if (fileName.startsWith("mhn_")) return "MHN";
        if (fileName.startsWith("er_")) return "ER";
        if (fileName.startsWith("gi_")) return "GI";
        return "unknown";
    }
}
