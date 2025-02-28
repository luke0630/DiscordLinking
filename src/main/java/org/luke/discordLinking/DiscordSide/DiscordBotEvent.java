package org.luke.discordLinking.DiscordSide;

import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.discordLinking.Data;
import org.luke.discordLinking.MojangAPI;
import org.luke.discordLinking.SQL.SQLManager;
import org.luke.discordLinking.SQL.SQLUtility;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static net.kyori.adventure.text.Component.text;
import static org.luke.discordLinking.DiscordLinking.getInstance;
import static org.luke.discordLinking.DiscordSide.DiscordBot.getMemberById;

public class DiscordBotEvent extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if(componentId.contains(":")) {
            String[] split = componentId.split(":");
            if(Objects.equals(split[0], "button_player")) {
                String uuid = split[1];
                boolean isSuccessful = SQLUtility.unlinkMinecraftAccount(event.getUser().getIdLong(), UUID.fromString(uuid));
                if(isSuccessful) {
                    String username = MojangAPI.getUsernameFromUUID(uuid);
                    event.reply(
                            "## **以下のアカウントとのリンクを解除しました。**" +
                            "\n```" + username + " (" + uuid + ")```"
                    ).setEphemeral(true).queue();
                }
                return;
            }
        }
        switch(componentId) {
            case "button_enter_code" -> {
                TextInput input = TextInput.create("input_code", "画面に表示されているコードを入力(コードの間にある空白を入れる必要はありません)", TextInputStyle.SHORT)
                        .setPlaceholder("コードの間にある空白を入れる必要はありません")
                        .setRequired(true)
                        .build();

                Modal modal = Modal.create("modal_submit", "コード入力")
                        .addActionRow(input)
                        .build();

                event.replyModal(modal).queue();
            }
            case "button_current_link" -> {
                JSONArray jsonArray = SQLUtility.getLinkedDataByDiscordID(event.getUser().getIdLong());
                if(jsonArray != null) {
                    StringBuilder replyString = new StringBuilder();
                    replyString.append("> 現在のリンク済みアカウント");
                    for(int i=0;i < jsonArray.length();i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        LocalDateTime dateTime = Timestamp.valueOf(jsonObject.getString("timestamp")).toLocalDateTime();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH時:mm分");
                        String formattedTimestamp = dateTime.format(formatter);

                        String uuid = jsonObject.getString("uuid");

                        String mcName = MojangAPI.getUsernameFromUUID(uuid);

                        replyString.append("\n```");
                        if(mcName != null) {
                            replyString.append("ユーザー名: ").append(mcName);
                        } else {
                            replyString.append("*ユーザー名を取得できませんでした。");
                        }
                        replyString
                                .append("\n\n(UUID: ")
                                .append(uuid)
                                .append(")")
                                .append("\n(リンク時刻: ")
                                .append(formattedTimestamp)
                                .append(")```")
                        ;
                    }

                    event.reply(replyString.toString()).setEphemeral(true).queue();
                } else {
                    event.reply("あなたのDiscordアカウントはリンクされていません。").setEphemeral(true).queue();
                }
            }
            case "button_unlink" -> {
                JSONArray jsonArray = SQLUtility.getLinkedDataByDiscordID(event.getUser().getIdLong());
                if(jsonArray != null) {
                    List<ActionComponent> buttons = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String uuid = jsonObject.getString("uuid");
                        String mcName = MojangAPI.getUsernameFromUUID(uuid);
                        String button_id = "button_player:" + uuid;
                        if(mcName != null) {
                            buttons.add(
                                    Button.success(button_id, mcName)
                            );
                        } else {
                            buttons.add(
                                    Button.success(button_id, "*名前を取得できませんでした。(UUID:" + uuid + ")")
                            );
                        }
                    }
                    event.reply("リンク解除したいマインクラフトアカウントを選択してください。")
                            .addActionRow(buttons)
                            .setEphemeral(true)
                            .queue();
                } else {
                    event.reply("あなたのDiscordアカウントはリンクされていません。").setEphemeral(true).queue();
                }
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        // モーダルのIDを確認
        if (event.getModalId().equals("modal_submit")) {

            ModalMapping input_code = event.getValue("input_code");
            if(input_code != null) {
                String inputText = input_code.getAsString();

                Guild guild = event.getJDA().getGuildById(Data.discordServerID);
                User user = event.getUser();
                var linkData = getInstance().getAuthCodeManager().verifyCode(inputText);
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

                    SQLUtility.putData(event.getUser().getIdLong(), linkData.getKey());

                    RoleAssigner.assignRole(user.getIdLong(), Data.discordLinkedRoleID, RoleAssigner.RoleMode.Add);
                    if (guild != null) {
                        ChangeDisplayNameOnDiscord(guild, user, minecraftUserName);
                    }
                } else {
                    String replyMessage = "# > ***入力されたコードでは認証できませんでした。***" +
                            "\n## **入力したコード: " + inputText + "**" +
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

    public static void Unlink(UUID minecraftuuid, Long discordLongID) {
        SQLManager.removeLinkData(minecraftuuid);
        RoleAssigner.assignRole(discordLongID, Data.discordLinkedRoleID, RoleAssigner.RoleMode.Remove);

        DiscordBot.getUserById(discordLongID, user -> {
            //If player is on mc server, kick player
            Optional<Player> optionalPlayer = getInstance().getServer().getPlayer(minecraftuuid);
            if (optionalPlayer.isPresent()) {
                Player player = optionalPlayer.get();
                player.disconnect(text(user.getGlobalName() + "#" + user.getName() + " とのリンクを解除しました。\n参加するにはリンクしなおしてください。"));
            }

            //Restore original displayName
            Guild guild = DiscordBot.getJda().getGuildById(Data.discordServerID);
            ChangeDisplayNameOnDiscord(guild, user, user.getGlobalName());
        });
    }

    private static void ChangeDisplayNameOnDiscord(Guild guild, User user, String changedName) {
        getMemberById(guild, user.getIdLong(), member -> {
            try {
                guild.modifyNickname(member, changedName).queue();
            } catch(Exception ignored) {}
        });
    }
}
