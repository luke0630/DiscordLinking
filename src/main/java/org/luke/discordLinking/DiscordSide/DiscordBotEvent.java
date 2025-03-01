package org.luke.discordLinking.DiscordSide;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.discordLinking.MojangAPI;
import org.luke.discordLinking.SQL.SQLUtility;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


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
        if (event.getModalId().equals("modal_submit")) {
            DiscordBotUtility.Link(event);
        }
    }
}
