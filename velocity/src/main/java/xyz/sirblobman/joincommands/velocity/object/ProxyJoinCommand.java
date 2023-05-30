package xyz.sirblobman.joincommands.velocity.object;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import xyz.sirblobman.joincommands.common.utility.Validate;
import xyz.sirblobman.joincommands.velocity.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.velocity.configuration.PlayerDataConfiguration;
import xyz.sirblobman.joincommands.velocity.configuration.VelocityConfiguration;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;

public final class ProxyJoinCommand {
    private final List<String> commandList;
    private final String permission;
    private final boolean firstJoinOnly;
    private final long delay;

    public ProxyJoinCommand(@NotNull List<String> commandList, @NotNull String permission, boolean firstJoinOnly,
                            long delay) {
        this.commandList = Validate.notEmpty(commandList, "commandList must not be empty!");
        this.permission = permission;

        this.firstJoinOnly = firstJoinOnly;
        this.delay = delay;
    }

    public @NotNull List<String> getCommands() {
        return Collections.unmodifiableList(this.commandList);
    }

    public @NotNull String getPermission() {
        return this.permission;
    }

    public boolean isFirstJoinOnly() {
        return this.firstJoinOnly;
    }

    public long getDelay() {
        return this.delay;
    }

    public boolean canExecute(@NotNull JoinCommandsPlugin plugin, @NotNull Player player) {
        if (isFirstJoinOnly() && hasJoinedBefore(plugin, player)) {
            return false;
        }

        String permission = getPermission();
        if (!permission.isEmpty()) {
            return player.hasPermission(this.permission);
        }

        return true;
    }

    public void execute(@NotNull JoinCommandsPlugin plugin, @NotNull Player player) {
        GameProfile gameProfile = player.getGameProfile();
        String playerName = gameProfile.getName();
        List<String> commandList = getCommands();

        for (String originalCommand : commandList) {
            String replacedCommand = originalCommand.replace("{player}", playerName);
            if (replacedCommand.startsWith("[PLAYER]")) {
                String playerCommand = replacedCommand.substring(8);
                runAsPlayer(plugin, player, playerCommand);
            } else {
                runAsConsole(plugin, replacedCommand);
            }
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
