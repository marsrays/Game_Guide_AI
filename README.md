# Game_Guide_AI
A game guide AI built with langchain4, Using a Telegram bot as a communication interface

## 重要設定項目
- `application.yml`：設定伺服器埠、Telegram token、Webhook 路徑、embedding/LLM provider 的 API 金鑰等（請參考 `TelegramBotConfig`、`EmbeddingConfig` 的欄位）。
- Telegram：
  - 設定 Bot Token（於 `application.yml`）。
  - 設定 HTTPS 的 webhook URL 指向 `TelegramWebhookController` 的路徑（或在開發時使用反向代理 / ngrok）。

## 主要功能與使用流程
1. 使用者透過 Telegram 傳送查詢或命令。
2. 程式透過 Controller 接收 webhook，交由 Service 處理會話與指令。
3. AI 組件會將查詢轉成 embedding，對內部 `EmbeddingStore` 搜索相似段落（可依 `Game` Enum 作為 metadata 過濾）。
4. 回傳格式化的攻略內容給使用者。

## 注意事項
- 確保 embedding 與儲存（vector store）已初始化並載入資料。
- 設定檔內的金鑰與 token 請不要放在版本控制中，使用環境變數或 CI 秘密管理。
- 若需本地測試 webhook，建議使用 `ngrok` 或類似工具暴露 HTTPS endpoint。

## 開發者
- GitHub 使用者：`marsrays`
- IDE：IntelliJ IDEA 2025.1.2（Windows）

## 授權
請參考專案根目錄的 `LICENSE`。
