package com.github.sirblobman.join.commands.bungee.object;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.join.commands.bungee.JoinCommandsBungee;

import org.jetbrains.annotations.NotNull;

public final class ProxyJoinCommand {
    private final List<String> commandList;
    private final String permission;
    private final boolean firstJoinOnly;
    private final long delay;

    public ProxyJoinCommand(List<String> commandList, String permission, boolean firstJoinOnly, long delay) {
        this.commandList = Validate.notEmpty(commandList, "commandList must not be empty!");
        this.permission = Validate.notNull(permission, "permission must not be null!");

        this.firstJoinOnly = firstJoinOnly;
        this.delay = delay;
    }

    @NotNull
    public List<String> getCommands() {
        return Collections.unmodifiableList(this.commandList);
    }

    @NotNull
    public String getPermission() {
        return this.permission;
    }

    public boolean isFirstJoinOnly() {
        return this.firstJoinOnly;
    }

    public long getDelay() {
        return this.delay;
    }

    public boolean shouldBeExecutedFor(JoinCommandsBungee plugin, ProxiedPlayer player) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");

        if(isFirstJoinOnly() && hasJoinedBefore(plugin, player)) {
            return false;
        }

        String permission = getPermission();
        if (!permission.isEmpty()) {
            return player.hasPermission(this.permission);
        }

        return true;
    }

    public void executeFor(JoinCommandsBungee plugin, ProxiedPlayer player) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");

        String playerName = player.getName();
        List<String> commandList = getCommands();
        for (String originalCommand : commandList) {
            String replacedCommand = originalCommand.replace("{player}", playerName);
            if(replacedCommand.startsWith("[PLAYER]")) {
                String playerCommand = replacedCommand.substring(8);
                runAsPlayer(plugin, player, playerCommand);
            } else {
                runAsConsole(plugin, replacedCommand);
            }
        }
    }

    private boolean hasJoinedBefore(JoinCommandsBungee plugin, ProxiedPlayer player) {
        Configuration configuration = plugin.getConfig();
        if(configuration.getBoolean("disable-player-data", false)) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        String playerIdString = playerId.toString();
        String path = ("joined-before." + playerIdString);
        return configuration.getBoolean(path, false);
    }

    private void runAsPlayer(JoinCommandsBungee plugin, ProxiedPlayer player, String command) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");
        Validate.notEmpty(command, "command must not be empty!");

        try {
            ProxyServer proxy = plugin.getProxy();
            PluginManager manager = proxy.getPluginManager();
            manager.dispatchCommand(player, command);
        } catch(Exception ex) {
            Logger logger = plugin.getLogger();
            String errorMessage = "An error occurred while executing command '/" + command + "' as a player:";
            logger.log(Level.WARNING, errorMessage, ex);
        }
    }

    private void runAsConsole(JoinCommandsBungee plugin, String command) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notEmpty(command, "command must not be empty!");

        try {
            ProxyServer proxy = plugin.getProxy();
            CommandSender console = proxy.getConsole();
            PluginManager manager = proxy.getPluginManager();
            manager.dispatchCommand(console, command);
        } catch(Exception ex) {
            Logger logger = plugin.getLogger();
            String errorMessage = "An error occurred while executing command '/" + command + "' in console:";
            logger.log(Level.WARNING, errorMessage, ex);
        }
    }
}
