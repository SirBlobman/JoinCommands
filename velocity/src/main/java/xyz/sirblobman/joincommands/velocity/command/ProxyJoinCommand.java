package xyz.sirblobman.joincommands.velocity.command;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import xyz.sirblobman.joincommands.common.command.JoinCommand;
import xyz.sirblobman.joincommands.common.utility.Validate;
import xyz.sirblobman.joincommands.velocity.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.velocity.configuration.PlayerDataConfiguration;
import xyz.sirblobman.joincommands.velocity.configuration.VelocityConfiguration;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;

public final class ProxyJoinCommand extends JoinCommand {
    public ProxyJoinCommand(@NotNull String id) {
        super(id);
    }

    public boolean canExecute(@NotNull JoinCommandsPlugin plugin, @NotNull Player player) {
        if (isFirstJoinOnly() && hasJoinedBefore(plugin, player)) {
            return false;
        }

        String permissionName = getPermissionName();
        if (permissionName == null || permissionName.isEmpty()) {
            return true;
        }

        return player.hasPermission(permissionName);
    }

    public void execute(@NotNull JoinCommandsPlugin plugin, @NotNull Player player) {
        String playerName = player.getGameProfile().getName();
        List<String> commandList = getCommandList();
        for (String command : commandList) {
            String replaced = command.replace("{player}", playerName);
            if (replaced.startsWith("[PLAYER]")) {
                String playerCommand = replaced.substring(8);
                runAsPlayer(plugin, player, playerCommand);
                continue;
            }

            runAsConsole(plugin, replaced);
        }
    }

    private boolean hasJoinedBefore(@NotNull JoinCommandsPlugin plugin, @NotNull Player player) {
        VelocityConfiguration configuration = plugin.getConfiguration();
        if (configuration.isDisablePlayerData()) {
            return false;
        }

        PlayerDataConfiguration playerData = plugin.getPlayerData();
        return playerData.hasPlayerJoinedBefore(player);
    }

    private void runAsPlayer(@NotNull JoinCommandsPlugin plugin, @NotNull Player player, @NotNull String command) {
        try {
            ProxyServer proxy = plugin.getServer();
            CommandManager commandManager = proxy.getCommandManager();
            commandManager.executeAsync(player, command);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            String errorMessage = "An error occurred while executing command '/" + command + "' as a player:";
            logger.log(Level.WARNING, errorMessage, ex);
        }
    }

    private void runAsConsole(@NotNull JoinCommandsPlugin plugin, @NotNull String command) {
        try {
            ProxyServer proxy = plugin.getServer();
            CommandManager commandManager = proxy.getCommandManager();
            ConsoleCommandSource console = proxy.getConsoleCommandSource();
            commandManager.executeAsync(console, command);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            String errorMessage = "An error occurred while executing command '/" + command + "' in console:";
            logger.log(Level.WARNING, errorMessage, ex);
        }
    }
}
