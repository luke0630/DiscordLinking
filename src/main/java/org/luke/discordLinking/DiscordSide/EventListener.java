package org.luke.discordLinking.DiscordSide;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import org.luke.discordLinking.Data;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.luke.discordLinking.DiscordLinking;
import org.luke.discordLinking.SQL.SQLManager;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.luke.discordLinking.Data.effectiveTimeForCode;


public class EventListener {

    public static Map<UUID, ScheduledExecutorService> schedulers = new HashMap<>();
    final String abc = "abcdefghijklmnpqrstuvwxyz"; //for code. doesn't include o because it is confusing with 0.

    @Subscribe
    public void LoginEvent(LoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Data.LinkedData linkedData = SQLManager.getLinkedData(uuid);

        //Check Linked or not
        if (linkedData != null) {
            //Linked
            Long discordUserID = linkedData.getDiscordUserID();
            DiscordBot.getUserById(discordUserID, user -> {
                DiscordLinking.getInstance().getServer().getScheduler().buildTask(DiscordLinking.getInstance(), () -> {
                            player.sendMessage(text("あなたのマイクラアカウントはDiscordアカウント(" + user.getGlobalName() + "#" + user.getName() + ")とリンクされています。", GREEN));
                            player.sendMessage(text("リンク先に見覚えがない場合は、/unlink コマンドでリンクし直してください。", RED));
                        })
                        .delay(1L, TimeUnit.SECONDS)
                        .schedule();
            });
        } else {
            //Not Linked
            StringBuilder code = new StringBuilder();
            Data.AuthData authData = Data.codeAuth.get(uuid);

            //Whether the code is being authenticated or not
            if (authData != null) {
                //Authenticating
                code = new StringBuilder(authData.getCode());
            } else {
                //not Authenticating
                //create random 5 chara code
                Random rand = new Random();
                for (int i = 0; i < 5; i++) {
                    //Decide whether the next character an abc or a number.
                    int isABC = rand.nextInt(0, 2);
                    if (isABC == 1) {
                        //If 1, an abc
                        int whichABC = rand.nextInt(0, abc.length());
                        String[] abcList = abc.split("");
                        code.append(abcList[whichABC]);
                    } else {
                        //if 0, a number
                        int num = rand.nextInt(0, 10);
                        code.append(num);
                    }
                }

                authData = new Data.AuthData(code.toString(), player, effectiveTimeForCode);
                Data.codeAuth.put(uuid, authData);

                StartCountDownForCode(authData, uuid);
            }

            player.disconnect(
                    text("参加するにはDiscordアカウントを連携させる必要があります。\n", RED)
                            .append(text("超生活鯖のディスコードサーバーの「連携方法」チャンネルをご覧ください。\n\n", WHITE))
                            .append(text("\n↓ディスコードサーバー招待URL↓\n", GREEN))
                            .append(text("https://discord.gg/hkP9PPetGV", AQUA))
                            .append(text("\n\nコード: " + code, WHITE))
                            .append(text("\n\nコードの有効時間 残り: " + authData.getResetTime() + "秒"))
                            .append(text("\n(もう一度参加しなおすと画面が更新されます)", GREEN))
                            .append(text("\nコードの有効時間を過ぎた場合もう一度サーバーに参加すれば新規コードが生成されます", RED))
            );
        }
    }


    public void StartCountDownForCode(Data.AuthData authData, UUID uuid) {
        //if already exists same code's scheduler, remove it.
        schedulers.remove(uuid);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            if (authData.getResetTime() <= 0) {
                System.out.println(uuid + "の認証コードは無効になりました。");
                Data.codeAuth.remove(uuid);
                scheduler.shutdown();
            } else {
                //count down
                authData.setResetTime(authData.getResetTime() - 1);
            }
        };

        //Run tasks at specified intervals（every sec）
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        schedulers.put(uuid, scheduler);
    }
}
