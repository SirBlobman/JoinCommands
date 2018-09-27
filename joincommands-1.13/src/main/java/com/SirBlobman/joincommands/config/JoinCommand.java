package com.SirBlobman.joincommands.config;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.SirBlobman.joincommands.JoinCommands;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class JoinCommand {
    private final String command, permission;
    private final long commandDelay;
    private final boolean usePermission, firstJoinOnly;
    public JoinCommand(String cmd, long delayInTicks, String permission, boolean firstJoinOnly) {
        this.command = cmd;
        this.commandDelay = delayInTicks;
        this.permission = permission;
        this.usePermission = (permission == null || permission.isEmpty()) ? false : true;
        this.firstJoinOnly = firstJoinOnly;
    }
    
    public boolean canExecute(Player p) {
        boolean execute = true;
        if(firstJoinOnly) {
            if(p.hasPlayedBefore()) execute = false;
            else execute = true;
        }
        
        if(usePermission) {
            if(p.hasPermission(permission)) execute = true;
            else execute = false;
        }
        
        return execute;
    }
    
    public void execute(Player p) {
    	Bukkit.getScheduler().runTaskLater(JoinCommands.INSTANCE, () -> {
            String cmd = command.replace("{player}", p.getName());
            if(cmd.startsWith("[PLAYER]")) {
                cmd = cmd.substring(8);
                execute(p, cmd);
            } else if(cmd.startsWith("[OP]")) {
                cmd = cmd.substring(4);
                if(p.isOp()) execute(p, cmd);
                else {
                    p.setOp(true);
                    execute(p, cmd);
                    p.setOp(false);
                }
            } else if(cmd.startsWith("[PBUNGEE]")) {
                try {
                    cmd = cmd.substring(9);
                    executeBungee(p, cmd, false);
                } catch(Throwable ex) {
                    String error = "You are not on a bungee server or there was an error:";
                    JoinCommands.log(error);
                    ex.printStackTrace();
                }
            } else if(cmd.startsWith("[CBUNGEE]")) {
                try {
                    cmd = cmd.substring(9);
                    executeBungee(p, cmd, true);
                } catch(Throwable ex) {
                    String error = "You are not on a bungee server or there was an error:";
                    JoinCommands.log(error);
                    ex.printStackTrace();
                }  
            } else {
                ConsoleCommandSender ccs = Bukkit.getConsoleSender();
                Bukkit.dispatchCommand(ccs, cmd);
            }
    	}, commandDelay);
    }
    
    private void executeBungee(Player p, String command, boolean console) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        String prefix = (console ? "CJC" : "PJC") + ":";
        out.writeUTF(prefix + command);
        p.sendPluginMessage(JoinCommands.INSTANCE, "joincommands:bungeecord", out.toByteArray());
    }
    
    private void execute(Player p, String command) {
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(p, "/" + command);
        Bukkit.getPluginManager().callEvent(event);
        if(!event.isCancelled()) p.performCommand(command);
    }
}