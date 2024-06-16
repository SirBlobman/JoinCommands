package xyz.sirblobman.joincommands.spigot.object;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.World;
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

public final class WorldJoinCommand {
    private final List<String> worldNameList;
    private final List<String> commandList;
    private final String permissionName;
    private final boolean firstJoinOnly;
    private final long delay;

    private transient Permission permission;

    public WorldJoinCommand(@NotNull List<String> worldNameList, @NotNull List<String> commandList,
                            @NotNull String permissionName, boolean firstJoinOnly, long delay) {
        Validate.notEmpty(commandList, "commandList must not be empty or null.");
        this.worldNameList = worldNameList;
        this.commandList = commandList;
        this.permissionName = permissionName;
        this.firstJoinOnly = firstJoinOnly;
        this.delay = delay;
    }

    private @NotNull List<String> getWorldNameList() {
        return Collections.unmodifiableList(this.worldNameList);
    }

    public @NotNull List<String> getCommands() {
        return Collections.unmodifiableList(this.commandList);
    }

    public @Nullable String getPermissionName() {
        return this.permissionName;
    }

    public boolean isFirstJoinOnly() {
        return this.firstJoinOnly;
    }

    public long getDelay() {
        return this.delay;
    }

    public @Nullable Permission getPermission() {
        if (this.permission != null) {
            return this.permission;
        }

        if (this.permissionName == null || this.permissionName.isEmpty()) {
            return null;
        }

        String permissionName = getPermissionName();
        String permissionDescription = "A permission that allows a specific join command to be executed.";
        this.permission = new Permission(permissionName, permissionDescription, PermissionDefault.FALSE);
        return this.permission;
    }

    public boolean canExecute(@NotNull JoinCommandsPlugin plugin, @NotNull Player player, @NotNull World world) {
        List<String> worldNameList = getWorldNameList();
        if (worldNameList.isEmpty()) {
            return false;
        }

        String worldName = world.getName();
        if (!worldNameList.contains("*") && !worldNameList.contains(worldName)) {
            return false;
        }

        if (isFirstJoinOnly() && hasJoinedBefore(plugin, player, world)) {
            return false;
        }

        Permission permission = getPermission();
        if (permission != null) {
            return player.hasPermission(permission);
        }

        return true;
    }

    public void execute(@NotNull JoinCommandsPlugin plugin, @NotNull Player player, @NotNull World world) {
        String playerName = player.getName();
        String worldName = world.getName();
        List<String> commandList = getCommands();

        for (String command : commandList) {
            String replaced = command.replace("{player}", playerName).replace("{world}", worldName);
            if (plugin.usePlaceholderAPIHook()) {
                replaced = PlaceholderAPI.setPlaceholders(player, replaced);
            }

            if (replaced.startsWith("[PLAYER]")) {
                String playerCommand = replaced.substring(8);
                runAsPlayer(player, playerCommand);
            } else if (replaced.startsWith("[OP]")) {
                String opCommand = replaced.substring(4);
                runAsOp(player, opCommand);
            } else if (replaced.startsWith("[BPLAYER]")) {
                String proxyPlayerCommand = replaced.substring(9);
                runAsProxyPlayer(plugin, player, proxyPlayerCommand);
            } else if (replaced.startsWith("[BCONSOLE]")) {
                String proxyConsoleCommand = replaced.substring(10);
                runAsProxyConsole(plugin, player, proxyConsoleCommand);
            } else {
                runAsConsole(replaced);
            }
        }
    }

    private boolean hasJoinedBefore(@NotNull JoinCommandsPlugin plugin, @NotNull Player player, @NotNull World world) {
        FileConfiguration configuration = plugin.getConfig();
        if (configuration.getBoolean("disable-player-data", false)) {
            return false;
        }

        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        YamlConfiguration playerData = playerDataManager.get(player);
        List<String> joinedWorldList = playerData.getStringList("join-commands.played-before-world-list");

        String worldName = world.getName();
        return joinedWorldList.contains(worldName);
    }

    private void runAsPlayer(@NotNull Player player, @NotNull String command) {
        if (command.isEmpty()) {
            return;
        }

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runAsOp(@NotNull Player player, @NotNull String command) {
        if (command.isEmpty()) {
            return;
        }

        if (player.isOp()) {
            runAsPlayer(player, command);
            return;
        }

        player.setOp(true);
        runAsPlayer(player, command);
        player.setOp(false);
    }

    private void runAsConsole(@NotNull String command) {
        if (command.isEmpty()) {
            return;
        }

        try {
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(console, command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void runAsProxyPlayer(@NotNull JoinCommandsPlugin plugin, @NotNull Player player, @NotNull String command) {
        if (command.isEmpty()) {
            return;
        }

        try {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF(command);

            byte[] message = dataOutput.toByteArray();
            player.sendPluginMessage(plugin, "jc:player", message);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            String messageFormat = "Failed to send a message on proxy channel 'jc:player':";
            logger.log(Level.WARNING, messageFormat, ex);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void runAsProxyConsole(@NotNull JoinCommandsPlugin plugin, @NotNull Player player, @NotNull String command) {
        if (command.isEmpty()) {
            return;
        }

        try {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF(command);

            byte[] message = dataOutput.toByteArray();
            player.sendPluginMessage(plugin, "jc:console", message);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            String messageFormat = "Failed to send a message on proxy channel 'jc:console':";
            logger.log(Level.WARNING, messageFormat, ex);
        }
    }
}
