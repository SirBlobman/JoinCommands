package com.github.sirblobman.join.commands.spigot.listener;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.join.commands.spigot.JoinCommandsSpigot;
import com.github.sirblobman.join.commands.spigot.manager.CommandManager;
import com.github.sirblobman.join.commands.spigot.object.ServerJoinCommand;
import com.github.sirblobman.join.commands.spigot.object.WorldJoinCommand;

public class ListenerJoinCommands implements Listener {
    private final JoinCommandsSpigot plugin;
    
    public ListenerJoinCommands(JoinCommandsSpigot plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        sendDebug("", "Detected PlayerJoinEvent...");
        
        Player player = e.getPlayer();
        sendDebug("Player Name: " + player.getName());
        
        sendDebug("Running server join commands for player...");
        runServerJoinComands(player);
        sendDebug("Finished running server join commands.");
        
        sendDebug("Setting player as previously joined if not already set.");
        setJoinedServerBefore(player);
        
        World world = player.getWorld();
        sendDebug("Detected world join for world " + world.getName());
        
        sendDebug("Running world join commands for player...");
        runWorldJoinCommands(player, world);
        sendDebug("Finished running world join commands.");
        
        setJoinedWorldBefore(player, world);
        sendDebug("Setting player as previously joined world if not already set.");
        
        sendDebug("Finished PlayerJoinEvent checks.");
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        sendDebug("", "Detected PlayerChangedWorldEvent...");
        Player player = e.getPlayer();
        sendDebug("Player Name: " + player.getName());
        World world = player.getWorld();
        sendDebug("World Name: " + world.getName());
        
        sendDebug("Running world join commands for player...");
        runWorldJoinCommands(player, world);
        sendDebug("Finished running world join commands.");
        
        setJoinedWorldBefore(player, world);
        sendDebug("Setting player as previously joined world if not already set.");
        
        sendDebug("Finished PlayerChangedWorldEvent checks.");
    }
    
    private void runServerJoinComands(Player player) {
        if(player == null) return;
        BukkitScheduler scheduler = Bukkit.getScheduler();
        
        CommandManager commandManager = this.plugin.getCommandManager();
        List<ServerJoinCommand> joinCommandList = commandManager.getJoinCommandList();
        joinCommandList.removeIf(command -> !command.shouldBeExecutedFor(this.plugin, player));
        
        for(ServerJoinCommand command : joinCommandList) {
            long delay = command.getDelay();
            Runnable task = () -> command.executeFor(this.plugin, player);
            scheduler.scheduleSyncDelayedTask(this.plugin, task, delay);
        }
    }
    
    private void runWorldJoinCommands(Player player, World world) {
        if(player == null || world == null) return;
        BukkitScheduler scheduler = Bukkit.getScheduler();
        
        CommandManager commandManager = this.plugin.getCommandManager();
        List<WorldJoinCommand> joinCommandList = commandManager.getWorldJoinCommandList();
        joinCommandList.removeIf(command -> !command.shouldBeExecutedFor(this.plugin, player, world));
        
        for(WorldJoinCommand command : joinCommandList) {
            long delay = command.getDelay();
            Runnable task = () -> command.executeFor(this.plugin, player);
            scheduler.scheduleSyncDelayedTask(this.plugin, task, delay);
        }
    }
    
    private void setJoinedServerBefore(Player player) {
        if(player == null) return;
        
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration config = playerDataManager.get(player);
        if(config.getBoolean("join-commands.played-before")) return;
        
        config.set("join-commands.played-before", true);
        playerDataManager.save(player);
    }
    
    private void setJoinedWorldBefore(Player player, World world) {
        if(player == null || world == null) return;
        String worldName = world.getName();
        
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration config = playerDataManager.get(player);
        
        List<String> worldList = config.getStringList("join-commands.played-before-world-list");
        if(worldList.contains(worldName)) return;
        worldList.add(worldName);
        
        config.set("join-commands.played-before-world-list", worldList);
        playerDataManager.save(player);
    }
    
    private void sendDebug(String... messageArray) {
        FileConfiguration configuration = this.plugin.getConfig();
        if(!configuration.getBoolean("debug-mode", false)) return;
        
        Logger logger = this.plugin.getLogger();
        for(String message : messageArray) {
            String logMessage = String.format(Locale.US, "[Debug] %s", message);
            logger.info(logMessage);
        }
    }
}
