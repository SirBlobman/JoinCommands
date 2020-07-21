package com.SirBlobman.join.commands.spigot;

import com.SirBlobman.api.configuration.PlayerDataManager;
import com.SirBlobman.join.commands.spigot.listener.ListenerJoinCommands;
import com.SirBlobman.join.commands.spigot.manager.CommandManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import org.bukkit.event.Listener;

public class JoinCommandsSpigot extends JavaPlugin {
    private final PlayerDataManager<JoinCommandsSpigot> playerDataManager;
    private final CommandManager commandManager;
    
    public JoinCommandsSpigot() {
        this.playerDataManager = new PlayerDataManager<>(this);
        this.commandManager = new CommandManager(this);
    }
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        
        registerBungeeCordChannels();
        registerListener();
    }
    
    @Override
    public void reloadConfig() {
        super.reloadConfig();
    
        CommandManager commandManager = getCommandManager();
        commandManager.loadServerJoinCommands();
        commandManager.loadWorldJoinCommands();
    }
    
    public PlayerDataManager<?> getPlayerDataManager() {
        return this.playerDataManager;
    }
    
    public CommandManager getCommandManager() {
        return this.commandManager;
    }
    
    private void registerBungeeCordChannels() {
        FileConfiguration config = getConfig();
        if(!config.getBoolean("spigot-options.bungeecord-hook")) return;
        
        Messenger messenger = Bukkit.getMessenger();
        messenger.registerOutgoingPluginChannel(this, "jc:console");
        messenger.registerOutgoingPluginChannel(this, "jc:player");
    }
    
    private void registerListener() {
        PluginManager manager = Bukkit.getPluginManager();
        Listener listener = new ListenerJoinCommands(this);
        manager.registerEvents(listener, this);
    }
}