package org.luke.discordLinking.Command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.luke.discordLinking.Data;
import org.luke.discordLinking.DiscordSide.DiscordBot;
import org.luke.discordLinking.DiscordSide.DiscordBotEvent;
import org.luke.discordLinking.SQL.SQLManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class Command_Linkinfo implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if(source instanceof Player player) {
            if(SQLManager.isLinkedUser(player.getUniqueId())) {
                Data.LinkedData linkedData = SQLManager.getLinkedData(player.getUniqueId());

                DiscordBot.getUserById(linkedData.getDiscordUserID(), user -> {
                    LocalDateTime linkedDate = linkedData.getLinkedLocalDateTime();
                    player.sendMessage(text("---------------------------------"));
                    player.sendMessage(text("現在のリンク情報", RED));
                    player.sendMessage(text(player.getUsername() + "("+ player.getUniqueId() +") ←→ " + user.getGlobalName() + "#" + user.getName(), RED));
                    player.sendMessage(text(linkedDate.getYear() + "年 " + linkedDate.getMonthValue() + "月 " + linkedDate.getDayOfMonth() + "日 / " + linkedDate.getHour() + "時:" + linkedDate.getMinute() + "分 にリンク済み", AQUA));
                    player.sendMessage(text("---------------------------------"));
                });
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
    }
}
