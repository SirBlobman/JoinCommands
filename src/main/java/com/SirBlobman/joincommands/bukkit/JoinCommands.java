package com.SirBlobman.joincommands.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.SirBlobman.api.nms.NMS_Handler;
import com.SirBlobman.joincommands.bukkit.config.Config;
import com.SirBlobman.joincommands.bukkit.listener.ListenPlayerJoin;

import java.util.logging.Logger;

public class JoinCommands extends JavaPlugin {
    public static JoinCommands INSTANCE;
    
    @Override
    public void onEnable() {
        INSTANCE = this;
        
        if(Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, getBungeeCordConsoleChannel());
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, getBungeeCordPlayerChannel());
        }
        
        Bukkit.getPluginManager().registerEvents(new ListenPlayerJoin(), this);
        
        Config.getJoinCommands(true);
        Config.getWorldJoinCommands(true);
    }
    
    public static void debug(String message) {
        Logger logger = INSTANCE.getLogger();
        logger.info("[Debug] " + message);
    }
    
    public String getBungeeCordConsoleChannel() {
        int minorVersion = NMS_Handler.getMinorVersion();
        if(minorVersion > 12) return "joincommands:bungee-console";
        
        return "jc:bc";
    }
    
    public String getBungeeCordPlayerChannel() {
        int minorVersion = NMS_Handler.getMinorVersion();
        if(minorVersion > 12) return "joincommands:bungee-player";
        
        return "jc:bp";
    }
}