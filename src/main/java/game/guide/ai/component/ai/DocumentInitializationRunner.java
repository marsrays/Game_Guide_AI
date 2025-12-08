package game.guide.ai.component.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@Slf4j
public class DocumentInitializationRunner implements ApplicationRunner {

    private final EmbeddingStoreIngestor ingestor;

    @Value("${docs.init.enabled:true}")
    private boolean initEnabled;

    @Value("${docs.path:src/main/resources/docs}")
    private String docsPath;

    public DocumentInitializationRunner(
            EmbeddingStoreIngestor ingestor,
            EmbeddingStore<TextSegment> embeddingStore) {
        this.ingestor = ingestor;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!initEnabled) {
            log.info("文檔初始化已禁用（docs.init.enabled=false）");
            return;
        }

        ingestDocuments();
    }

    private void ingestDocuments() {
        log.info("開始導入文檔，路徑: {}", docsPath);
        long startTime = System.currentTimeMillis();

        try {
            Path path = Paths.get(docsPath);

            if (!Files.exists(path)) {
                log.warn("文檔路徑不存在: {}", docsPath);
                return;
            }

            List<Document> documents = FileSystemDocumentLoader.loadDocuments(docsPath);

            if (documents.isEmpty()) {
                log.warn("未找到任何文檔");
                return;
            }

            log.info("找到 {} 個文檔，開始向量化...", documents.size());
            ingestor.ingest(documents);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✓ 文檔導入完成！共 {} 個文檔，耗時 {} ms",
                    documents.size(), duration);

        } catch (Exception e) {
            log.error("✗ 文檔導入失敗", e);
            throw new RuntimeException("文檔初始化失敗", e);
        }
    }
}
