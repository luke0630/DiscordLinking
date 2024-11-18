package org.luke.discordLinking.Command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.luke.discordLinking.Data;
import org.luke.discordLinking.DiscordSide.DiscordBot;
import org.luke.discordLinking.DiscordSide.DiscordBotEvent;
import org.luke.discordLinking.SQL.SQLManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Command_Unlink implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if(source instanceof Player player) {
            if(SQLManager.isLinkedUser(player.getUniqueId())) {
                Data.LinkedData linkedData = SQLManager.getLinkedData(player.getUniqueId());
                Long discordLongID = linkedData.getDiscordUserID();

                DiscordBotEvent.Unlink(player.getUniqueId() ,discordLongID);
                DiscordBot.getUserById(discordLongID, user -> {
                    user.openPrivateChannel().queue((MessageChannel channel) -> {
                        channel.sendMessage("あなたのDiscordアカウントとマインクラフトアカウント(" + player.getUsername() +")とのリンクは解除されました。").queue();
                    });
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
