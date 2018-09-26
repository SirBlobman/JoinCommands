package com.SirBlobman.joincommands;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.SirBlobman.joincommands.config.Config;
import com.SirBlobman.joincommands.config.JoinCommand;
import com.SirBlobman.joincommands.config.WorldJoinCommand;

/*
 * Join Command Format:
 * command1:
 *   permission: 'jc.command1'
 *   command: 'msg {player} Welcome to the server!'
 *   first join only: true
 *   delay: 20
 *   
 * Set the permission to '' to not use it
 */
public class JoinCommands extends JavaPlugin implements Listener {
    private static final Logger LOG = Logger.getLogger("JoinCommands");
    public static JoinCommands INSTANCE;
    public static File FOLDER;
    
    @Override
    public void onEnable() {
        INSTANCE = this;
        FOLDER = getDataFolder();
        Config.load();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BC-JoinCommands");
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    
    public static void log(String... ss) {
        for(String s : ss) LOG.info(s);
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        for(JoinCommand jc : Config.getCommands(false)) {
            if(jc.canExecute(player)) jc.execute(player);
        }
        
        for(WorldJoinCommand wjc : Config.getWorldCommands(false)) {
            if(wjc.canExecute(player)) wjc.execute(player);
        }
    }
    
    @EventHandler
    public void onJoinWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        for(WorldJoinCommand wjc : Config.getWorldCommands(false)) {
            if(wjc.canExecute(player)) wjc.execute(player);
        }
    }
}