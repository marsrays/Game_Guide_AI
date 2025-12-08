package game.guide.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage {
    private String text;
    private String imageUrl;
    private LocalDateTime timestamp;
    private String userName;
    private Long chatId;

    public boolean isExpired(int timeoutMinutes) {
        return timestamp.isBefore(LocalDateTime.now().minusMinutes(timeoutMinutes));
    }
}