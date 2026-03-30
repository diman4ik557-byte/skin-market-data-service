package by.step.entity.enums;

import lombok.Getter;

@Getter
public enum SocialPlatform {

    VK("ВКонтакте", "https://vk.com/"),
    TELEGRAM("Telegram", "https://t.me/"),
    INSTAGRAM("Instagram", "https://instagram.com/"),
    DISCORD("Discord", "https://discord.gg/"),
    NAMEMC("NameMC","https://namemc.com/profile/");

    private final String displayName;
    private final String urlPrefix;

    SocialPlatform(String displayName, String urlPrefix) {
        this.displayName = displayName;
        this.urlPrefix = urlPrefix;
    }
}
