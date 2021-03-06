package com.github.sirblobman.join.commands.spigot.object;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.join.commands.spigot.JoinCommandsSpigot;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.Validate;

public class ServerJoinCommand {
    private final List<String> commandList;
    private final String permission;
    private final boolean firstJoinOnly;
    private final long delay;
    public ServerJoinCommand(List<String> commandList, String permission, boolean firstJoinOnly, long delay) {
        Validate.notEmpty(commandList, "commandList must not be empty or null.");
        
        this.commandList = commandList;
        this.permission = permission;
        this.firstJoinOnly = firstJoinOnly;
        this.delay = delay;
    }
    
    public long getDelay() {
        return this.delay;
    }
    
    public boolean shouldBeExecutedFor(JoinCommandsSpigot plugin, Player player) {
        if(plugin == null || player == null) return false;
        
        if(this.firstJoinOnly) {
            PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
            YamlConfiguration configuration = playerDataManager.get(player);
            
            boolean hasJoinedBefore = configuration.getBoolean("join-commands.played-before", false);
            if(hasJoinedBefore) return false;
        }
        
        if(this.permission != null && !this.permission.isEmpty()) {
            Permission permission = new Permission(this.permission, "A permission that allows a specific server join command to be executed.", PermissionDefault.FALSE);
            return player.hasPermission(permission);
        }
        
        return true;
    }
    
    public void executeFor(JoinCommandsSpigot plugin, Player player) {
        if(plugin == null || player == null) return;
        String playerName = player.getName();
        
        for(String command : this.commandList) {
            command = command.replace("{player}", playerName);

            if(plugin.usePlaceholderAPIHook()) {
                command = PlaceholderAPI.setPlaceholders(player, command);
            }
            
            if(command.toLowerCase().startsWith("[player]")) {
                command = command.substring("[player]".length());
                runAsPlayer(player, command);
            }
            
            else if(command.toLowerCase().startsWith("[op]")) {
                command = command.substring("[op]".length());
                runAsOp(player, command);
            }
            
            else if(command.toLowerCase().startsWith("[bplayer]")) {
                command = command.substring("[bplayer]".length());
                runAsBungeePlayer(plugin, player, command);
            }
            
            else if(command.toLowerCase().startsWith("[bconsole]")) {
                command = command.substring("[bconsole]".length());
                runAsBungeeConsole(plugin, player, command);
            }

            else {
                runAsConsole(command);
            }
        }
    }
    
    private void runAsPlayer(Player player, String command) {
        if(player == null || command == null || command.isEmpty()) return;
        PluginManager manager = Bukkit.getPluginManager();
        
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
        manager.callEvent(event);
        if(event.isCancelled()) return;
        
        String actualCommand = event.getMessage().substring(1);
        player.performCommand(actualCommand);
    }
    
    private void runAsOp(Player player, String command) {
        if(player == null || command == null || command.isEmpty()) return;
        
        if(player.isOp()) {
            runAsPlayer(player, command);
            return;
        }
        
        player.setOp(true);
        runAsPlayer(player, command);
        player.setOp(false);
    }
    
    private void runAsConsole(String command) {
        if(command == null || command.isEmpty()) return;
        
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, command);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void runAsBungeePlayer(JoinCommandsSpigot plugin, Player player, String command) {
        if(plugin == null || player == null || command == null || command.isEmpty()) return;
        
        try {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF(command);
            byte[] message = dataOutput.toByteArray();
            
            player.sendPluginMessage(plugin, "jc:player", message);
        } catch(Exception ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while sending a message on channel 'jc:player'. Is the BungeeCord proxy online?", ex);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void runAsBungeeConsole(JoinCommandsSpigot plugin, Player player, String command) {
        if(plugin == null || player == null || command == null || command.isEmpty()) return;
        
        try {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF(command);
            byte[] message = dataOutput.toByteArray();
            player.sendPluginMessage(plugin, "jc:console", message);
        } catch(Exception ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while sending a message on channel 'jc:console'. Is the BungeeCord proxy online?", ex);
        }
    }
}