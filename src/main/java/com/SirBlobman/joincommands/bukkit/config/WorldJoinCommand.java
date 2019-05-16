package com.SirBlobman.joincommands.bukkit.config;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.SirBlobman.joincommands.bukkit.JoinCommands;

import java.sql.SQLException;
import java.util.List;

public class WorldJoinCommand extends JoinCommand {
    private final List<String> validWorldList;
    private final boolean firstJoinOnly;
    public WorldJoinCommand(List<String> validWorldList, String command, long delayInTicks, String permission, boolean firstJoinOnly) {
        super(command, delayInTicks, permission, false);
        this.firstJoinOnly = firstJoinOnly;
        this.validWorldList = validWorldList;
    }
    
    @Override
    public boolean canBeExecuted(Player player) {
        if(!super.canBeExecuted(player)) return false;
        if(this.validWorldList.contains("*")) return true;
        
        World world = player.getWorld();
        String worldName = world.getName();
        if(!this.validWorldList.contains(worldName)) return false;
        
        if(this.firstJoinOnly) {
            try {
                List<String> joinedWorldList = SQLiteUtil.getJoinedWorlds(player);
                if(joinedWorldList.contains(worldName)) return false;
            } catch (SQLException ex) {
                JoinCommands.INSTANCE.getLogger().info("Failed to get world join list for player '" + player.getName() + "'.");
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public void execute(Player player) {
        if(player == null) return;
        
        String command = this.command.replace("{world}", player.getWorld().getName());
        new JoinCommand(command, this.commandDelay, this.permission, this.firstJoinOnly).execute(player);
    }
}