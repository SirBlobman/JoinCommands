package com.SirBlobman.joincommands.bukkit.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.SirBlobman.joincommands.bukkit.config.Config;
import com.SirBlobman.joincommands.bukkit.config.JoinCommand;
import com.SirBlobman.joincommands.bukkit.config.WorldJoinCommand;

import java.util.List;

public class ListenPlayerJoin implements Listener {
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onJoinServer(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        
        List<JoinCommand> commandList = Config.getJoinCommands(false);
        for(JoinCommand command : commandList) {
            if(command.canBeExecuted(player)) command.execute(player);
        }
        
        checkWorld(player);
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onJoinWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        checkWorld(player);
    }
    
    public void checkWorld(Player player) {
        List<WorldJoinCommand> commandList = Config.getWorldJoinCommands(false);
        for(WorldJoinCommand command : commandList) {
            if(command.canBeExecuted(player)) command.execute(player);
        }
    }
}