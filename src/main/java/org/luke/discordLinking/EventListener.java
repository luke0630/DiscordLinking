package org.luke.discordLinking;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
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
            String code;
            AuthData authData = getInstance().getAuthCodeManager().getPlayersAuthData(uuid);

            // コード入力待ちかどうか
            if (authData != null) {
                code = authData.getCode();
            } else {
                authData = getInstance().getAuthCodeManager().generateAuthCode(event.getPlayer());
                code = authData.getCode();
            }

            long left_expiration = (authData.getExpirationTime() - System.currentTimeMillis()) / 1000;

            player.disconnect(
                    text("参加するにはDiscordアカウントを連携させる必要があります。\n", RED)
                            .append(text("超生活鯖のディスコードサーバーの「連携方法」チャンネルをご覧ください。\n\n", WHITE))
                            .append(text("\n\nコード: " + code, WHITE))
                            .append(text("\n\nコードの有効時間 残り: " + left_expiration + "秒"))
                            .append(text("\n(もう一度参加しなおすと画面が更新されます)", GREEN))
                            .append(text("\nコードの有効時間を過ぎた場合もう一度サーバーに参加すれば新規コードが生成されます", RED))
            );
        }
    }
}
