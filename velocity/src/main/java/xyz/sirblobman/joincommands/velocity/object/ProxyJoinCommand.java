package xyz.sirblobman.joincommands.velocity.object;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import xyz.sirblobman.joincommands.common.utility.Validate;
import xyz.sirblobman.joincommands.velocity.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.velocity.configuration.PlayerDataConfiguration;
import xyz.sirblobman.joincommands.velocity.configuration.VelocityConfiguration;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;
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

    public boolean shouldBeExecutedFor(JoinCommandsPlugin plugin, Player player) {
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

    public void executeFor(JoinCommandsPlugin plugin, Player player) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");

        GameProfile gameProfile = player.getGameProfile();
        String playerName = gameProfile.getName();
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

    private boolean hasJoinedBefore(JoinCommandsPlugin plugin, Player player) {
        VelocityConfiguration configuration = plugin.getConfiguration();
        if(configuration.isDisablePlayerData()) {
            return false;
        }

        PlayerDataConfiguration playerData = plugin.getPlayerData();
        return playerData.hasPlayerJoinedBefore(player);
    }

    private void runAsPlayer(JoinCommandsPlugin plugin, Player player, String command) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");
        Validate.notEmpty(command, "command must not be empty!");

        try {
            ProxyServer proxy = plugin.getServer();
            CommandManager commandManager = proxy.getCommandManager();
            commandManager.executeAsync(player, command);
        } catch(Exception ex) {
            Logger logger = plugin.getLogger();
            String errorMessage = "An error occurred while executing command '/" + command + "' as a player:";
            logger.log(Level.WARNING, errorMessage, ex);
        }
    }

    private void runAsConsole(JoinCommandsPlugin plugin, String command) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notEmpty(command, "command must not be empty!");

        try {
            ProxyServer proxy = plugin.getServer();
            CommandManager commandManager = proxy.getCommandManager();
            ConsoleCommandSource console = proxy.getConsoleCommandSource();
            commandManager.executeAsync(console, command);
        } catch(Exception ex) {
            Logger logger = plugin.getLogger();
            String errorMessage = "An error occurred while executing command '/" + command + "' in console:";
            logger.log(Level.WARNING, errorMessage, ex);
        }
    }
}
