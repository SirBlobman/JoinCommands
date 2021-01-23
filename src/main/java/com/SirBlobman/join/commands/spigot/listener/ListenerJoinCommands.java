package com.SirBlobman.join.commands.spigot.listener;

import com.SirBlobman.api.configuration.PlayerDataManager;
import com.SirBlobman.join.commands.spigot.JoinCommandsSpigot;
import com.SirBlobman.join.commands.spigot.manager.CommandManager;
import com.SirBlobman.join.commands.spigot.object.ServerJoinCommand;
import com.SirBlobman.join.commands.spigot.object.WorldJoinCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;

public class ListenerJoinCommands implements Listener {
    private final JoinCommandsSpigot plugin;
    public ListenerJoinCommands(JoinCommandsSpigot plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        runServerJoinComands(player);
        setJoinedServerBefore(player);
        
        World world = player.getWorld();
        runWorldJoinCommands(player, world);
        setJoinedWorldBefore(player, world);
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        World world = player.getWorld();
        
        runWorldJoinCommands(player, world);
        setJoinedWorldBefore(player, world);
    }
    
    private void runServerJoinComands(Player player) {
        if(player == null) return;
        BukkitScheduler scheduler = Bukkit.getScheduler();
        
        CommandManager commandManager = this.plugin.getCommandManager();
        List<ServerJoinCommand> joinCommandList = commandManager.getJoinCommandList();
        
        for(ServerJoinCommand command : joinCommandList) {
            if(!command.shouldBeExecutedFor(this.plugin, player)) continue;
            
            Runnable task = () -> command.executeFor(this.plugin, player);
            long delay = command.getDelay();
            scheduler.scheduleSyncDelayedTask(this.plugin, task, delay);
        }
    }
    
    private void runWorldJoinCommands(Player player, World world) {
        if(player == null || world == null) return;
        BukkitScheduler scheduler = Bukkit.getScheduler();
        
        CommandManager commandManager = this.plugin.getCommandManager();
        List<WorldJoinCommand> joinCommandList = commandManager.getWorldJoinCommandList();
    
        for(WorldJoinCommand command : joinCommandList) {
            if(!command.shouldBeExecutedFor(this.plugin, player, world)) continue;
    
            Runnable task = () -> command.executeFor(this.plugin, player);
            long delay = command.getDelay();
            scheduler.scheduleSyncDelayedTask(this.plugin, task, delay);
        }
    }
    
    private void setJoinedServerBefore(Player player) {
        if(player == null) return;
    
        PlayerDataManager<?> playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration config = playerDataManager.getData(player);
        if(config.getBoolean("join-commands.played-before")) return;
        
        config.set("join-commands.played-before", true);
        playerDataManager.saveData(player);
    }
    
    private void setJoinedWorldBefore(Player player, World world) {
        if(player == null || world == null) return;
        String worldName = world.getName();
        
        PlayerDataManager<?> playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration config = playerDataManager.getData(player);
        
        List<String> worldList = config.getStringList("join-commands.played-before-world-list");
        if(worldList.contains(worldName)) return;
        worldList.add(worldName);
        
        config.set("join-commands.played-before-world-list", worldList);
        playerDataManager.saveData(player);
    }
}