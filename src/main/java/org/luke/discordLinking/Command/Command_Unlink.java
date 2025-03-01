package org.luke.discordLinking.Command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.luke.discordLinking.SQL.SQLUtility;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class Command_Unlink implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if(source instanceof  Player player) {
            String discordID = SQLUtility.getDiscordIdByUUID(player.getUniqueId());
            player.sendMessage(
                    text("リンク解除を行っています...", AQUA)
            );
            if(!SQLUtility.unlinkMinecraftAccount(Long.valueOf(discordID), player.getUniqueId())) {
                player.sendMessage(text("リンク解除に失敗しました。", RED));
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
