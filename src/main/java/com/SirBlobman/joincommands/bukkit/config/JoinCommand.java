package com.SirBlobman.joincommands.bukkit.config;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.SirBlobman.joincommands.bukkit.JoinCommands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class JoinCommand {
    protected final String command, permission;
    protected final boolean usePermission, firstJoinOnly;
    protected final long commandDelay;
    public JoinCommand(String command, long delayInTicks, String permission, boolean firstJoinOnly) {
        this.command = command;
        this.commandDelay = delayInTicks;
        this.permission = permission;
        this.usePermission = (permission != null && permission.isEmpty());
        this.firstJoinOnly = firstJoinOnly;
    }
    
    public boolean canBeExecuted(Player player) {
        if(player == null) return false;
        
        if(this.firstJoinOnly && player.hasPlayedBefore()) return false;
        if(this.usePermission && !player.hasPermission(this.permission)) return false;
        
        return true;
    }
    
    public void execute(Player player) {
        if(player == null) return;
        
        Runnable task = () -> {
            String command = this.command.replace("{player}", player.getName());
            if(command.startsWith("[PLAYER]")) {
                command = command.substring(8);
                runAsPlayer(player, command);
                return;
            }
            
            if(command.startsWith("[OP]")) {
                command = command.substring(4);
                if(player.isOp()) {
                    runAsPlayer(player, command);
                    return;
                }
                
                player.setOp(true);
                runAsPlayer(player, command);
                player.setOp(false);
                return;
            }
            
            if(command.startsWith("[PBUNGEE]")) {
                command = command.substring(9);
                runOnBungee(player, command, false);
                return;
            }
            
            if(command.startsWith("[CBUNGEE]")) {
                command = command.substring(9);
                runOnBungee(player, command, true);
                return;
            }
            
            CommandSender console = Bukkit.getConsoleSender();
            try {Bukkit.dispatchCommand(console, command);}
            catch(Exception ex) {
                JoinCommands.INSTANCE.getLogger().warning("An error occurred while executing the command '" + command + "'.");
                ex.printStackTrace();
            }
        };
        Bukkit.getScheduler().runTaskLater(JoinCommands.INSTANCE, task, this.commandDelay);
    }
    
    private void runAsPlayer(Player player, String command) {
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        
        try{
            command = event.getMessage();
            player.performCommand(command);
        } catch(Exception ex) {
            JoinCommands.INSTANCE.getLogger().warning("An error occurred while executing the command '" + command + "'.");
            ex.printStackTrace();
        }
    }
    
    private void runOnBungee(Player player, String command, boolean asConsole) {
        try {
            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeUTF(command);
            byte[] message = output.toByteArray();
            
            String channel = asConsole ? JoinCommands.INSTANCE.getBungeeCordConsoleChannel() : JoinCommands.INSTANCE.getBungeeCordPlayerChannel();
            player.sendPluginMessage(JoinCommands.INSTANCE, channel, message);
        } catch(Exception ex) {
            JoinCommands.INSTANCE.getLogger().warning("Failed to execute bungee command, is bungeecord linked correctly?");
            ex.printStackTrace();
        }
    }
}