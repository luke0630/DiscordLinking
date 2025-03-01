package org.luke.discordLinking;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.format.TextDecoration;
import org.luke.discordLinking.Auth.AuthData;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.luke.discordLinking.DiscordSide.DiscordBot;
import org.luke.discordLinking.SQL.SQLUtility;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.luke.discordLinking.DiscordLinking.getInstance;


public class EventListener {
    @Subscribe
    public void LoginEvent(LoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String discordID = SQLUtility.getDiscordIdByUUID(uuid);
        if (discordID != null) {
            DiscordBot.getUserById(Long.valueOf(discordID), user -> {
                getInstance().getServer().getScheduler().buildTask(getInstance(), () -> {
                            player.sendMessage(text("あなたのマイクラアカウントはDiscordアカウント(" + user.getGlobalName() + "#" + user.getName() + ")とリンクされています。", GREEN));
                            player.sendMessage(text("リンク先に見覚えがない場合は、/unlink コマンドでリンクし直してください。", RED));
                        })
                        .delay(1L, TimeUnit.SECONDS)
                        .schedule();
            });
        } else {
            StringBuilder code = new StringBuilder();
            AuthData authData = getInstance().getAuthCodeManager().getPlayersAuthData(uuid);

            // 接続する度にコードを生成し、今までのコードを削除する
            if(authData != null) {
                getInstance().getAuthCodeManager().removeCode(authData.getCode());
            }
            authData = getInstance().getAuthCodeManager().generateAuthCode(event.getPlayer());

            code.append(authData.getCode());

            long left_expiration = (authData.getExpirationTime() - System.currentTimeMillis()) / 1000;
            Timestamp timestamp = new Timestamp(authData.getExpirationTime());
            LocalDateTime timestamp_localDateTime = timestamp.toLocalDateTime();

            String left_expiration_string =
                    String.format(
                            "%s時:%s分:%s秒",
                            timestamp_localDateTime.getHour(),
                            timestamp_localDateTime.getMinute(),
                            timestamp_localDateTime.getSecond()
                    );


            int center_of_code = code.length() / 2;
            code.insert(center_of_code, " ");

            player.disconnect(
                    text("", RED)
                            .append(text("超生活鯖のDiscordサーバー内「連携方法」チャンネルをご確認ください。", AQUA))
                            .append(text("\n\nコード", GOLD))
                            .append(text("\n" + code, WHITE))
                            .append(text("\n\nコードの有効時間", RED))
                            .append(text("\n" + left_expiration_string + "(" + left_expiration + "秒後)に", WHITE))
                            .append(text("\n無効化されます。", WHITE))
                            .append(text("\n\n※ 参加し直すたびに新しいコードが発行され、", GREEN))
                            .append(text("\n以前のコードは無効になります。", GREEN))
                            .append(text("\n\n※ コード内の空白は入力しないでください。", GREEN))
            );
        }
    }
}
