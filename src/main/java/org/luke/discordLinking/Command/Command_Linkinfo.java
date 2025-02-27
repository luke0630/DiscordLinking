package org.luke.discordLinking.Command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.luke.discordLinking.DiscordSide.DiscordBot;
import org.luke.discordLinking.SQL.SQLUtility;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class Command_Linkinfo implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if(source instanceof Player player) {
            String discordID = SQLUtility.getDiscordIdByUUID(player.getUniqueId());
            if(discordID != null) {
                DiscordBot.getUserById(Long.valueOf(discordID), user -> {
                    player.sendMessage(text("---------------------------------"));
                    player.sendMessage(text("現在のリンク情報", RED));
                    player.sendMessage(text(user.getGlobalName() + "#" + user.getName(), RED));
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
