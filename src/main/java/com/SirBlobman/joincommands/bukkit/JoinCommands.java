package com.SirBlobman.joincommands.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.SirBlobman.joincommands.bukkit.config.Config;
import com.SirBlobman.joincommands.bukkit.listener.ListenPlayerJoin;

import java.util.logging.Logger;

public class JoinCommands extends JavaPlugin {
    public static JoinCommands INSTANCE;
    
    @Override
    public void onEnable() {
        INSTANCE = this;
        
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "joincommands:bungee-console");
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "joincommands:bungee-player");
        Bukkit.getPluginManager().registerEvents(new ListenPlayerJoin(), this);
        
        Config.getJoinCommands(true);
        Config.getWorldJoinCommands(true);
    }
    
    public static void debug(String message) {
        Logger logger = INSTANCE.getLogger();
        logger.info("[Debug] " + message);
    }
}