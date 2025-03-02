package org.luke.discordLinking.Auth;

import com.velocitypowered.api.proxy.Player;
import org.luke.discordLinking.Data;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.luke.discordLinking.DiscordLinking.getInstance;

public class AuthCodeManager {
    private final Map<UUID, AuthData> authDataMap = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final SecureRandom random = new SecureRandom();
    private final char[] abcList = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final int chara_count = 8;

    public AuthCodeManager() {
        startCleanupTask();
    }

    private final Set<String> usedCodes = new HashSet<>();

    public AuthData generateAuthCode(Player player) {
        StringBuilder code = new StringBuilder(chara_count);

        while (true) {
            code.setLength(0);

            for (int i = 0; i < chara_count; i++) {
                code.append(abcList[random.nextInt(abcList.length)]);
            }

            if (!usedCodes.contains(code.toString())) {
                usedCodes.add(code.toString());
                break;
            }
        }

        AuthData authData = new AuthData(code.toString(), player.getUsername(), Data.effectiveTimeForCode);
        authDataMap.put(player.getUniqueId(), authData);
        return authData;
    }

    public AuthData getPlayersAuthData(UUID uuid) {
        return authDataMap.get(uuid);
    }

    public void startCleanupTask() {
        Runnable task = () -> authDataMap.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                getInstance().getLogger().info(
                        "{} ({}) の認証コードは無効になりました",
                        entry.getValue().getPlayer_displayName(),
                        entry.getKey()
                );
                return true;
            }
            return false;
        });

        // 期限切れ処理を最適化し、定期的に実行
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
    }

    public Map.Entry<UUID, AuthData> verifyCode(String inputCode) {
        var authData = getAuthDataFromCode(sanitizeAuthCode(inputCode));
        if(authData != null) {
            authDataMap.remove(authData.getKey());
            return authData;
        } else {
            return null;
        }
    }

    public void removeCode(String code) {
        var map = getAuthDataFromCode(code);
        if(map != null) {
            authDataMap.remove(map.getKey());
        }
    }

    private String sanitizeAuthCode(String inputCode) {
        return inputCode.replaceAll("[^a-zA-Z0-9]", "");
    }


    private Map.Entry<UUID, AuthData> getAuthDataFromCode(String code) {
        for(var entry : authDataMap.entrySet()) {
            if(entry.getValue().getCode().equals(code)) {
                return entry;
            }
        }
        return null;
    }
}
