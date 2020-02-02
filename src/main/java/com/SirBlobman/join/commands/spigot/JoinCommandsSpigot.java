package com.SirBlobman.join.commands.spigot;

import com.SirBlobman.api.SirBlobmanAPI;
import com.SirBlobman.join.commands.spigot.listener.ListenerJoinCommands;
import com.SirBlobman.join.commands.spigot.manager.CommandManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

public class JoinCommandsSpigot extends JavaPlugin {
    private final SirBlobmanAPI sirBlobmanAPI = new SirBlobmanAPI(this);
    private final CommandManager commandManager = new CommandManager(this);
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
    
        CommandManager commandManager = getCommandManager();
        commandManager.loadServerJoinCommands();
        commandManager.loadWorldJoinCommands();
        
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
    
    public SirBlobmanAPI getSirBlobmanAPI() {
        return this.sirBlobmanAPI;
    }
    
    public CommandManager getCommandManager() {
        return this.commandManager;
    }
    
    private void registerBungeeCordChannels() {
        FileConfiguration config = getConfig();
        if(!config.getBoolean("options.bungeecord-hook")) return;
        
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