package org.luke.discordLinking.DiscordSide;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.luke.discordLinking.Data;
import org.luke.discordLinking.MojangAPI;
import org.luke.discordLinking.SQL.SQLUtility;

import java.util.UUID;

import static org.luke.discordLinking.DiscordLinking.getInstance;
import static org.luke.discordLinking.DiscordSide.DiscordBot.getMemberById;

@UtilityClass
public class DiscordBotUtility {
    public void sendMessageToUser(User user, String message) {
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
    }

    public void sendUnlinkedMessage(User user, UUID targetUUID) {
        String userName = MojangAPI.getUsernameFromUUID(targetUUID.toString());
        String message =
                "## **リンクを解除しました**" +
                "\n解除したマインクラフトアカウント" +
                "\n```\n" +
                userName + " (" + targetUUID + ")" +
                "\n```";

        sendMessageToUser(user, message);
    }
    public void sendLinkedMessage(User user, UUID targetUUID) {
        String userName = MojangAPI.getUsernameFromUUID(targetUUID.toString());
        String message =
                "## **リンクしました**" +
                "\nリンク先のマインクラフトアカウント" +
                        "\n```\n" +
                        userName + " (" + targetUUID + ")" +
                        "\n```";

        sendMessageToUser(user, message);
    }
    public void deleteOwnMessages(TextChannel channel) {
        channel.getIterableHistory().queue(messages -> {
            for (Message message : messages) {
                if (message.getAuthor().isBot()) {
                    message.delete().queue();
                }
            }
        });
    }

    public void ChangeDisplayNameOnDiscord(Guild guild, User user, String changedName) {
        getMemberById(guild, user.getIdLong(), member -> {
            try {
                guild.modifyNickname(member, changedName).queue();
            } catch(Exception ignored) {}
        });
    }

    public void Link(ModalInteractionEvent event) {
        User user = event.getUser();
        ModalMapping input_code = event.getValue("input_code");
        if(input_code != null) {
            String code = input_code.getAsString();
            var linkData = getInstance().getAuthCodeManager().verifyCode(code);
            if(linkData != null) {
                String minecraftUserName = linkData.getValue().getPlayer_displayName();
                String replyMessage = "以下のマインクラフトアカウントとあなたのDiscordアカウントをリンクしました。" +
                        "```" +
                        "\n" +
                        "ユーザー名: " + minecraftUserName +
                        "\n" +
                        "UUID: " + linkData.getKey() +
                        "```" +
                        "\nこのマインクラフトアカウントでログインするとサーバーに参加可能です。";
                event.reply(replyMessage).setEphemeral(true).queue();
                DiscordBotUtility.sendLinkedMessage(event.getUser(), linkData.getKey());

                SQLUtility.putData(event.getUser().getIdLong(), linkData.getKey());

                RoleAssigner.assignRole(user.getIdLong(), Data.discordLinkedRoleID, RoleAssigner.RoleMode.Add);
                DiscordBotUtility.ChangeDisplayNameOnDiscord(DiscordBot.getGuild(), user, minecraftUserName);
            } else {
                String replyMessage = "# > ***入力されたコードでは認証できませんでした。***" +
                        "\n## **入力したコード: " + code + "**" +
                        "\n" +
                        "\n以下注意してもう一度お試しください。" +
                        "```" +
                        "\n* 0とo   1とl   bとd  などの似た文字" +
                        "\n" +
                        "\n* 有効期限が切れている(切れている場合、再度サーバーにアクセスすると新しいコードが表示されます)" +
                        "\n" +
                        "\n* 半角英数字で入力してください" +
                        "\n" +
                        "```";
                event.reply(replyMessage).setEphemeral(true).queue();
            }
        }
    }
}
