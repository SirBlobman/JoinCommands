package com.SirBlobman.join.commands.bungee.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.SirBlobman.join.commands.bungee.JoinCommandsBungee;
import com.SirBlobman.join.commands.bungee.object.ProxyJoinCommand;

import net.md_5.bungee.config.Configuration;

public class CommandManager {
    private final JoinCommandsBungee plugin;
    private final List<ProxyJoinCommand> proxyJoinCommandList = new ArrayList<>();
    public CommandManager(JoinCommandsBungee plugin) {
        this.plugin = plugin;
    }
    
    public void loadProxyJoinCommands() {
        this.proxyJoinCommandList.clear();
        
        Logger logger = this.plugin.getLogger();
        Configuration config = this.plugin.getConfig();
        if(config == null) return;
    
        Configuration section = config.getSection("proxy-join-commands");
        if(section == null) return;
    
        Collection<String> commandIdList = section.getKeys();
        for(String commandId : commandIdList) {
            if(commandId == null || commandId.isEmpty()) continue;
            
            Configuration commandSection = section.getSection(commandId);
            if(commandSection == null) continue;
            
            ProxyJoinCommand proxyJoinCommand = loadProxyJoinCommand(commandId, commandSection);
            if(proxyJoinCommand == null) continue;
            
            this.proxyJoinCommandList.add(proxyJoinCommand);
        }
    }
    
    public List<ProxyJoinCommand> getProxyJoinCommandList() {
        return new ArrayList<>(this.proxyJoinCommandList);
    }
    
    private ProxyJoinCommand loadProxyJoinCommand(String commandId, Configuration section) {
        if(section == null) return null;
    
        List<String> commandList = section.getStringList("command-list");
        String permission = section.getString("permission");
        boolean firstJoinOnly = section.getBoolean("first-join-only");
        long delay = section.getLong("delay");
    
        try {
            return new ProxyJoinCommand(commandList, permission, firstJoinOnly, delay);
        } catch(Exception ex) {
            Logger logger = this.plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while loading the proxy join command with id '" + commandId + "':", ex);
            return null;
        }
    }
}