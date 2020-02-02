package com.SirBlobman.join.commands.spigot.object;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.SirBlobman.api.SirBlobmanAPI;
import com.SirBlobman.join.commands.spigot.JoinCommandsSpigot;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang.Validate;

public class WorldJoinCommand {
    private final List<String> worldNameList, commandList;
    private final String permission;
    private final boolean firstJoinOnly;
    private final long delay;
    public WorldJoinCommand(List<String> worldNameList, List<String> commandList, String permission, boolean firstJoinOnly, long delay) {
        Validate.notNull(worldNameList, "worldNameList must not be null.");
        Validate.notEmpty(commandList, "commandList must not be empty or null.");
        Validate.notNull(permission, "permission must not be null.");
        
        this.worldNameList = worldNameList;
        this.commandList = commandList;
        this.permission = permission;
        this.firstJoinOnly = firstJoinOnly;
        this.delay = delay;
    }
    
    public long getDelay() {
        return this.delay;
    }
    
    public boolean shouldBeExecutedFor(JoinCommandsSpigot plugin, Player player, World world) {
        if(plugin == null || player == null || world == null || this.worldNameList.isEmpty()) return false;
        String worldName = world.getName();
        
        if(this.firstJoinOnly) {
            SirBlobmanAPI api = plugin.getSirBlobmanAPI();
            YamlConfiguration config = api.getDataFile(player);
            
            List<String> joinedWorldNameList = config.getStringList("join-commands.played-before-world-list");
            if(joinedWorldNameList.contains(worldName)) return false;
        }
        
        if(!this.permission.isEmpty()) {
            Permission permission = new Permission(this.permission, "A permission that allows a specific world join command to be executed.", PermissionDefault.FALSE);
            if(!player.hasPermission(permission)) return false;
        }
        
        return (this.worldNameList.contains("*") || this.worldNameList.contains(worldName));
    }
    
    public void executeFor(JoinCommandsSpigot plugin, Player player) {
        if(plugin == null || player == null) return;
        String playerName = player.getName();
        
        for(String command : this.commandList) {
            command = command.replace("{player}", playerName);
            
            if(command.toLowerCase().startsWith("[player]")) {
                command = command.substring("[player]".length());
                runAsPlayer(player, command);
                continue;
            }
            
            if(command.toLowerCase().startsWith("[op]")) {
                command = command.substring("[op]".length());
                runAsOp(player, command);
                continue;
            }
            
            if(command.toLowerCase().startsWith("[bplayer]")) {
                command = command.substring("[bplayer]".length());
                runAsBungeePlayer(plugin, player, command);
                continue;
            }
            
            if(command.toLowerCase().startsWith("[bconsole]")) {
                command = command.substring("[bconsole]".length());
                runAsBungeeConsole(plugin, player, command);
                continue;
            }
            
            runAsConsole(command);
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