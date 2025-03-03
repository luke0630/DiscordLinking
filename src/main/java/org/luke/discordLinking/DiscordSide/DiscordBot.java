package org.luke.discordLinking.DiscordSide;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.luke.discordLinking.Data;

import java.util.function.Consumer;

import static org.luke.discordLinking.DiscordLinking.getInstance;

public class DiscordBot {
    @Getter
    private static JDA jda = null;
    @Getter
    private static TextChannel channel;
    @Getter
    private static Guild guild;

    final static ItemComponent[] INIT_MESSAGE_COMPONENT = {
            Button.success("button_enter_code", "コードを入力"),
            Button.primary("button_current_link", "リンク状況を確認"),
            Button.danger("button_unlink", "リンクを解除する")
    };

    final static String INIT_MESSAGE =
            "```ansi\n" +
            "\u001B[0;1;32;1mコードを入力\u001B[0m サーバー接続時に表示されるコードを入力してリンクする\n" +
            "\u001B[0;1;34;1mリンク状況を確認\u001B[0m リンク状況を確認する\n" +
            "\u001B[0;1;31;1mリンクを解除する\u001B[0m リンクされているアカウントを選択しそれをリンク解除する\n" +
            "```";

    public static void MainClass() {
        jda = JDABuilder.createDefault(Data.discordBotID)
                .setRawEventsEnabled(true)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DiscordBotEvent())
                .setActivity(Activity.playing("鯖管理"))
                .build();

        try {
            jda.awaitReady();

            guild = jda.getGuildById(Data.discordServerID);
            if(guild == null) {
                getInstance().getLogger().error("サーバーを取得できませんでした。サーバーIDが間違っている可能性があります");
                return;
            }

            channel = guild.getTextChannelById(Data.discordChannelID);
            if(channel == null) {
                getInstance().getLogger().error("テキストチャンネルが見つかりませんでした。チャンネルIDが間違っている可能性があります");
            } else {
                Member botMember = guild.getSelfMember();
                if (!botMember.hasPermission(channel, Permission.MESSAGE_SEND)) {
                    getInstance().getLogger().error("Bot にメッセージ送信権限がありません！");
                    return;
                }

                DiscordBotUtility.deleteOwnMessages(channel, () -> {
                    String initMessage =
                            "***コードを入力 ->*** マイクラ鯖アクセス時に表示されるコードを入力してリンクする" +
                                    "\n***リンク状況を確認 ->*** 現在リンクされているアカウントの一覧を確認" +
                                    "\n***リンクを解除する ->*** アカウントを選択してそのアカウントとのリンクを解除する";
                    channel.sendMessage(initMessage)
                            .addActionRow(
                                    Button.success("button_enter_code", "コードを入力"),
                                    Button.primary("button_current_link", "リンク状況を確認"),
                                    Button.danger("button_unlink", "リンクを解除する")
                            )
                            .setSuppressedNotifications(true)
                            .queue( message -> selectOptionMessage = message );
                });
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    // Use load because guild.getMemberById is cached
    public static void getUserById(Long userId, Consumer<User> callback) {
        jda.retrieveUserById(userId).queue(callback, failure -> getInstance().getLogger().warn("ユーザーが見つかりませんでした。id: {}", userId));
    }

    // Use load because guild.getMemberById is cached
    public static void getMemberById(Guild guild, Long id, Consumer<Member> callback) {
        guild.loadMembers().onSuccess(members -> {
            for (Member member : members) {
                if(member.getUser().getIdLong() == id) {
                    callback.accept(member);
                    return;
                }
            }
            callback.accept(null);
        });
    }
}