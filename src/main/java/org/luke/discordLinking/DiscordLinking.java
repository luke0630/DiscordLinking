package org.luke.discordLinking;

import com.google.inject.Inject;
import com.sun.net.httpserver.HttpServer;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.luke.discordLinking.Auth.AuthCodeManager;
import org.luke.discordLinking.Command.Command_Linkinfo;
import org.luke.discordLinking.Command.Command_Unlink;
import org.luke.discordLinking.DiscordSide.DiscordBot;
import org.luke.discordLinking.DiscordSide.EventListener;
import org.luke.discordLinking.SQL.SQLManager;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Plugin(
        id = "discordlinking",
        name = "DiscordLinking",
        version = "1.0"
)
public class DiscordLinking {
    @Getter
    private final ProxyServer server;
    private final Logger logger;
    public static YamlDocument settingConfig;
    private static DiscordLinking instance;

    @Getter
    private AuthCodeManager authCodeManager;

    @Inject
    public DiscordLinking(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;

        instance = this;

        authCodeManager = new AuthCodeManager();

        //load setting config data
        Load(dataDirectory);
        //connect to database
        SQLManager.ConnectionToDatabase();
        SQLManager.CreateDatabase();

        DiscordBot.MainClass();

        //register commands
        CommandManager commandManager = server.getCommandManager();
        Map<CommandMeta.Builder, SimpleCommand> commands = Map.of(
                commandManager.metaBuilder("unlink"), new Command_Unlink(),
                commandManager.metaBuilder("linkinfo"), new Command_Linkinfo()
        );

        for(var entry : commands.entrySet()) {
            commandManager.register(entry.getKey().build(), entry.getValue());
        }
    }

    private void Load(Path dataDirectory) {
        try {
            settingConfig = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml"))
            );
        } catch (IOException ignored) {}

        Data.discordLinkedRoleID = settingConfig.getLong("discord-server-linked-role-id");
        Data.discordServerID = settingConfig.getLong("discord-server-long-id");
        Data.discordChannelID = settingConfig.getLong("discord-server-channel-id");
        Data.discordBotID = settingConfig.getString("discord-server-bot-id");
        Data.mysqlURL = settingConfig.getString("mysql-url");
        Data.mysqlUserName = settingConfig.getString("mysql-username");
        Data.mysqlPassword = settingConfig.getString("mysql-password");
        Data.mysqlDatabaseName = settingConfig.getString("mysql-database-name");
        Data.effectiveTimeForCode = settingConfig.getInt("code-effective-time-sec");
    }

    public static DiscordLinking getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DiscordLinking instance has not been initialized yet.");
        }
        return instance;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getEventManager().register(this, new EventListener());
    }
}

