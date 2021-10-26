package com.github.sirblobman.join.commands.bungee.object;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

import com.github.sirblobman.join.commands.bungee.JoinCommandsBungee;

public class ProxyJoinCommand {
    private final List<String> commandList;
    private final String permission;
    private final boolean firstJoinOnly;
    private final long delay;
    
    public ProxyJoinCommand(List<String> commandList, String permission, boolean firstJoinOnly, long delay) {
        this.commandList = Objects.requireNonNull(commandList, "commandList must not be null!");
        if(this.commandList.isEmpty()) throw new IllegalArgumentException("commandList must no be empty!");
        this.permission = Objects.requireNonNull(permission, "permission must not be null!");
        
        this.firstJoinOnly = firstJoinOnly;
        this.delay = delay;
    }
    
    public long getDelay() {
        return this.delay;
    }
    
    public boolean shouldBeExecutedFor(JoinCommandsBungee plugin, ProxiedPlayer player) {
        if(plugin == null || player == null) return false;
        
        if(this.firstJoinOnly) {
            Configuration config = plugin.getConfig();
            UUID uuid = player.getUniqueId();
            String uuidString = uuid.toString();
            
            boolean hasJoinedBefore = config.getBoolean("joined-before." + uuidString, false);
            if(hasJoinedBefore) return false;
        }
        
        if(!this.permission.isEmpty()) {
            return player.hasPermission(this.permission);
        }
        
        return true;
    }
    
    public void executeFor(JoinCommandsBungee plugin, ProxiedPlayer player) {
        if(plugin == null || player == null) return;
        String playerName = player.getName();
        
        for(String command : this.commandList) {
            command = command.replace("{player}", playerName);
            
            if(command.toLowerCase().startsWith("[player]")) {
                command = command.substring("[player]".length());
                runAsPlayer(plugin, player, command);
                continue;
            }
            
            runAsConsole(plugin, command);
        }
    }
    
    private void runAsPlayer(JoinCommandsBungee plugin, ProxiedPlayer player, String command) {
        if(plugin == null || player == null || command == null || command.isEmpty()) return;
        ProxyServer proxy = plugin.getProxy();
        
        PluginManager manager = proxy.getPluginManager();
        manager.dispatchCommand(player, command);
    }
    
    private void runAsConsole(JoinCommandsBungee plugin, String command) {
        if(plugin == null || command == null || command.isEmpty()) return;
        
        ProxyServer proxy = plugin.getProxy();
        CommandSender console = proxy.getConsole();
        
        PluginManager manager = proxy.getPluginManager();
        manager.dispatchCommand(console, command);
    }
}
