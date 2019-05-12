package com.SirBlobman.joincommands.bukkit.config;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class WorldJoinCommand extends JoinCommand {
    private final List<String> validWorldList;
    public WorldJoinCommand(List<String> validWorldList, String command, long delayInTicks, String permission, boolean firstJoinOnly) {
        super(command, delayInTicks, permission, firstJoinOnly);
        this.validWorldList = validWorldList;
    }
    
    @Override
    public boolean canBeExecuted(Player player) {
        if(!super.canBeExecuted(player)) return false;
        if(this.validWorldList.contains("*")) return true;
        
        World world = player.getWorld();
        String worldName = world.getName();
        return this.validWorldList.contains(worldName);
    }
    
    @Override
    public void execute(Player player) {
        if(player == null) return;
        
        String command = this.command.replace("{world}", player.getWorld().getName());
        new JoinCommand(command, this.commandDelay, this.permission, this.firstJoinOnly).execute(player);
    }
}