package org.luke.discordLinking;

import com.velocitypowered.api.proxy.Player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class Data {

    public static Long discordLinkedRoleID = null;
    public static Long discordServerID = null;
    public static String discordBotID = "";
    public static String mysqlURL = "";
    public static String mysqlUserName = "";
    public static String mysqlPassword = "";
    public static String mysqlDatabaseName = "";
    public static Integer effectiveTimeForCode = 300;

    public static HashMap<UUID, AuthData> codeAuth = new HashMap<>();

    public static class LinkedData {
        private Long discordUserID = null;

        public LinkedData(Long discordUserID, LocalDateTime nowDateTime) {
            this.discordUserID = discordUserID;
            linkedLocalDateTime = nowDateTime;
        }

        private final LocalDateTime linkedLocalDateTime;

        public LocalDateTime getLinkedLocalDateTime() {
            return linkedLocalDateTime;
        }
        public Long getDiscordUserID() {
            return discordUserID;
        }
    }

    public static class AuthData {
        private String code = "";
        private Player player = null;
        private Integer resetTime = 5*60; //秒単位 これを過ぎるとコードが無効になる

        public AuthData(String code, Player player, Integer restTime) {
            this.code = code;
            this.player = player;
            this.resetTime = restTime;
        }

        public void setResetTime(Integer resetTime) {
            this.resetTime = resetTime;
        }

        public Integer getResetTime() {
            return resetTime;
        }

        public Player getPlayer() {
            return player;
        }

        public String getCode() {
            return code;
        }
    }
}
