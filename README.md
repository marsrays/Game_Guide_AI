# Game_Guide_AI
A game guide AI built with langchain4, Using a Telegram bot as a communication interface

## 重要設定項目
- `application.yml`：設定伺服器埠、Telegram token、Webhook 路徑、embedding/LLM provider 的 API 金鑰等（請參考 `TelegramBotConfig`、`EmbeddingConfig` 的欄位）。
- Telegram：
  - 設定 Bot Token（於 `application.yml`）。
  - 設定 HTTPS 的 webhook URL 指向 `TelegramWebhookController` 的路徑（或在開發時使用反向代理 / ngrok）。

## Technology Stack
- Java 17+
- Spring Boot 4.0.0
- LangChain4j 1.9.1
- Maven
- Lombok
- Telegram Bots Spring Boot Starter 6.9.7.1

## 主要功能與使用流程
1. 使用者透過 Telegram 傳送查詢或命令。
2. 程式透過 Controller 接收 webhook，交由 Service 處理會話與指令。
3. AI 組件會將查詢轉成 embedding，對內部 `EmbeddingStore` 搜索相似段落（可依 `Game` Enum 作為 metadata 過濾）。
4. 回傳格式化的攻略內容給使用者。

## 工作流程
```text
用戶提問 
    ↓
┌─────────────────────────────────────┐
│ 第一支 AI：Intent Analysis AI        │
│ - 判斷是否為支援的遊戲                │
│ - 提取遊戲名稱                       │
│ - 理解問題內容                       │
│ - 缺漏資訊時追問用戶                  │
└─────────────────────────────────────┘
    ↓ (當資訊完整且遊戲支援時)
┌─────────────────────────────────────┐
│  第二支AI：Game Guide AI             │
│  - 使用 Tool 搜索向量庫              │
│  - 根據遊戲名稱動態過濾               │
│  - 提供攻略答案                      │
└─────────────────────────────────────┘
    ↓
返回答案給用戶
```

## 注意事項
- 確保 embedding 與儲存（vector store）已初始化並載入資料。
- 設定檔內的金鑰與 token 請不要放在版本控制中，使用環境變數或 CI 秘密管理。
- 若需本地測試 webhook，建議使用 `ngrok` 或類似工具暴露 HTTPS endpoint。
- 基於快速啟動並測試，目前記憶使用本地暫存，考量到之後分散式服務可多台同時啟用，使用 Webhook 與 Telegram Bot 聯繫。
- 多台同時啟用務必更換本地暫存為 redis 等遠端暫存，避免AI記憶與用戶狀態混亂
- 若只用於個人使用，建議更換成 LongPolling Bot，可省去建立 HTTPS endpoint 的麻煩。

## 開發者
- GitHub 使用者：`marsrays`
- IDE：IntelliJ IDEA 2025.1.2（Windows）

## 授權
請參考專案根目錄的 `LICENSE`。
