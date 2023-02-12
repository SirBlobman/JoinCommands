package xyz.sirblobman.joincommands.spigot.object;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import xyz.sirblobman.joincommands.common.utility.Validate;
import xyz.sirblobman.joincommands.spigot.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.spigot.manager.PlayerDataManager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ServerJoinCommand {
    private final List<String> commandList;
    private final String permissionName;
    private final boolean firstJoinOnly;
    private final long delay;

    private transient Permission permission;

    public ServerJoinCommand(List<String> commandList, String permissionName, boolean firstJoinOnly, long delay) {
        Validate.notEmpty(commandList, "commandList must not be empty or null.");

        this.commandList = commandList;
        this.permissionName = permissionName;
        this.firstJoinOnly = firstJoinOnly;
        this.delay = delay;
    }

    @NotNull
    public List<String> getCommands() {
        return Collections.unmodifiableList(this.commandList);
    }

    @Nullable
    public String getPermissionName() {
        return this.permissionName;
    }

    public boolean isFirstJoinOnly() {
        return this.firstJoinOnly;
    }

    public long getDelay() {
        return this.delay;
    }

    public Permission getPermission() {
        if (this.permissionName == null || this.permissionName.isEmpty()) {
            return null;
        }

        if (this.permission == null) {
            String permissionName = getPermissionName();
            String permissionDescription = "A permission that allows a specific join command to be executed.";
            this.permission = new Permission(permissionName, permissionDescription, PermissionDefault.FALSE);
        }

        return this.permission;
    }

    public boolean shouldBeExecutedFor(JoinCommandsPlugin plugin, Player player) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");

        if(isFirstJoinOnly() && hasJoinedBefore(plugin, player)) {
            return false;
        }

        Permission permission = getPermission();
        if (permission != null) {
            return player.hasPermission(permission);
        }

        return true;
    }

    public void executeFor(JoinCommandsPlugin plugin, Player player) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");

        String playerName = player.getName();
        List<String> commandList = getCommands();

        for (String originalCommand : commandList) {
            String replacedCommand = originalCommand.replace("{player}", playerName);
            if(plugin.usePlaceholderAPIHook()) {
                replacedCommand = PlaceholderAPI.setPlaceholders(player, replacedCommand);
            }

            if(replacedCommand.startsWith("[PLAYER]")) {
                String playerCommand = replacedCommand.substring(8);
                runAsPlayer(player, playerCommand);
            } else if(replacedCommand.startsWith("[OP]")) {
                String opCommand = replacedCommand.substring(4);
                runAsOp(player, opCommand);
            } else if(replacedCommand.startsWith("[BPLAYER]")) {
                String bplayerCommand = replacedCommand.substring(9);
                runAsBungeePlayer(plugin, player, bplayerCommand);
            } else if(replacedCommand.startsWith("[BCONSOLE]")) {
                String bconsoleCommand = replacedCommand.substring(10);
                runAsBungeeConsole(plugin, player, bconsoleCommand);
            } else {
                runAsConsole(replacedCommand);
            }
        }
    }

    private boolean hasJoinedBefore(JoinCommandsPlugin plugin, Player player) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");

        FileConfiguration configuration = plugin.getConfig();
        if(configuration.getBoolean("disable-player-data", false)) {
            return false;
        }

        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        YamlConfiguration playerData = playerDataManager.get(player);
        return playerData.getBoolean("join-commands.played-before", false);
    }

    private void runAsPlayer(Player player, String command) {
        Validate.notNull(player, "player must not be null!");
        Validate.notEmpty(command, "command must not be empty!");

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        try {
            String eventMessage = event.getMessage();
            String actualCommand = eventMessage.substring(1);
            player.performCommand(actualCommand);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runAsOp(Player player, String command) {
        Validate.notNull(player, "player must not be null!");
        Validate.notEmpty(command, "command must not be empty!");

        if (player.isOp()) {
            runAsPlayer(player, command);
            return;
        }

        try {
            player.setOp(true);
            runAsPlayer(player, command);
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            player.setOp(false);
        }
    }

    private void runAsConsole(String command) {
        Validate.notEmpty(command, "command must not be empty!");

        try {
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(console, command);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void runAsBungeePlayer(JoinCommandsPlugin plugin, Player player, String command) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");
        Validate.notEmpty(command, "command must not be empty!");

        try {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF(command);

            byte[] message = dataOutput.toByteArray();
            player.sendPluginMessage(plugin, "jc:player", message);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while sending a message on channel 'jc:player'. " +
                    "Is the BungeeCord proxy online?", ex);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void runAsBungeeConsole(JoinCommandsPlugin plugin, Player player, String command) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(player, "player must not be null!");
        Validate.notEmpty(command, "command must not be empty!");

        try {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF(command);

            byte[] message = dataOutput.toByteArray();
            player.sendPluginMessage(plugin, "jc:console", message);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while sending a message on channel 'jc:console'. " +
                    "Is the BungeeCord proxy online?", ex);
        }
    }
}
