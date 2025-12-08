package game.guide.ai.util;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.output.TokenUsage;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * ThreadLocal holder for response metadata including token usage and model information
 */
@UtilityClass
public class MetadataHolder {

    private static final ThreadLocal<ResponseMetadata> metadataContext = new ThreadLocal<>();

    public static void setMetadata(ResponseMetadata metadata) {
        metadataContext.set(metadata);
    }

    public static ResponseMetadata getMetadata() {
        return metadataContext.get();
    }

    public static void clear() {
        metadataContext.remove();
    }

    // OpenAI pricing map (price per 1M tokens in USD)
    private static final Map<String, PricingInfo> OPENAI_PRICING = Map.ofEntries(
            Map.entry("gpt-4.1", new PricingInfo(2.00, 8.00)),
            Map.entry("gpt-4.1-2025-04-14", new PricingInfo(2.00, 8.00)),
            Map.entry("gpt-4.1-mini", new PricingInfo(0.40, 1.60)),
            Map.entry("gpt-4.1-mini-2025-04-14", new PricingInfo(0.40, 1.60))
    );

    public static String formatMetadata() {
        ResponseMetadata metadata = getMetadata();
        if (metadata == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // 添加模型資訊
        if (metadata.getModelName() != null) {
            sb.append("\n\n🤖 使用模型: ").append(metadata.getModelName());
        }

        // 添加 Token 使用統計
        TokenUsage usage = metadata.getTokenUsage();
        if (usage != null) {
            sb.append("\n📊 Token使用統計:");
            int inputTokens = usage.inputTokenCount() != null ? usage.inputTokenCount() : 0;
            int outputTokens = usage.outputTokenCount() != null ? usage.outputTokenCount() : 0;
            int totalTokens = usage.totalTokenCount() != null ? usage.totalTokenCount() : 0;

            sb.append("\n• 輸入: ").append(inputTokens).append(" tokens");
            sb.append("\n• 輸出: ").append(outputTokens).append(" tokens");
            sb.append("\n• 總計: ").append(totalTokens).append(" tokens");

            // 計算費用 (只針對 OpenAI)
//            if (metadata.getModelProvider() != null &&
//                    "OPENAI".equals(metadata.getModelProvider().toString()) &&
//                    metadata.getModelName() != null) {
//                PricingInfo pricing = OPENAI_PRICING.get(metadata.getModelName());
//                if (pricing != null) {
//                    double inputCost = (inputTokens / 1_000_000.0) * pricing.inputPrice;
//                    double outputCost = (outputTokens / 1_000_000.0) * pricing.outputPrice;
//                    double totalCost = inputCost + outputCost;
//
//                    sb.append("\n💰 費用估算:");
//                    sb.append(String.format("\n• 輸入: $%.6f", inputCost));
//                    sb.append(String.format("\n• 輸出: $%.6f", outputCost));
//                    sb.append(String.format("\n• 總計: $%.6f", totalCost));
//                }
//            }
        }

        return sb.toString();
    }

    @Data
    @Builder
    public static class ResponseMetadata {
        private TokenUsage tokenUsage;
        private String modelName;
        private ModelProvider modelProvider;
    }

    private record PricingInfo(double inputPrice, double outputPrice) {
    }
}