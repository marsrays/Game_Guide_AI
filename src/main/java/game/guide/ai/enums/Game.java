package game.guide.ai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum Game {
    MHN("魔物獵人 Now", "Monster Hunter Now", "mhn"),
    ER("艾爾登法環", "Elden Ring", "elden", "er"),
    GI("原神", "Genshin Impact", "genshin", "gi"),
    HSR("崩壞星穹鐵道", "Honkai Star Rail", "star rail", "hsr"),
    ZZZ("絕區零", "Zenless Zone Zero", "zzz"),
    ZELDA("薩爾達傳說", "The Legend of Zelda", "zelda"),
    POKEMON("寶可夢", "Pokemon", "pokemon");

    private final String chineseName;
    private final String englishName;
    private final String[] aliases;

    Game(String chineseName, String englishName, String... aliases) {
        this.chineseName = chineseName;
        this.englishName = englishName;
        this.aliases = aliases;
    }

    /**
     * 獲取完整的遊戲名稱（中英文）
     */
    public String getFullName() {
        return chineseName + " (" + englishName + ")";
    }
}

