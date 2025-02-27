package org.luke.discordLinking.Auth;

import lombok.Getter;

@Getter
public class AuthData {
    private final String code;
    private final long expirationTime;
    private final String player_displayName;

    public AuthData(String code, String player_displayName, long expirationDurationInSeconds) {
        this.code = code;
        this.expirationTime = System.currentTimeMillis() + expirationDurationInSeconds * 1000; // ミリ秒単位で設定
        this.player_displayName = player_displayName;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}
