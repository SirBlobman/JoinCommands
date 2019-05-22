package com.SirBlobman.joincommands.bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class JoinCommands extends Plugin implements Listener {
    @Override
    public void onEnable() {
        ProxyServer server = ProxyServer.getInstance();
        
        server.registerChannel("joincommands:bungee-console");
        server.registerChannel("jc:bc");
        
        server.registerChannel("joincommands:bungee-player");
        server.registerChannel("jc:bp");
        
        server.getPluginManager().registerListener(this, this);
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onMessage(PluginMessageEvent e) {
        String tag = e.getTag();
        if(!tag.startsWith("joincommands:") && !tag.startsWith("jc:")) return;
        
        try {
            byte[] data = e.getData();
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            
            String line = dis.readUTF();
            getLogger().info("Incoming message in channel '" + tag + "': '" + line + "'");
            
            ProxyServer server = ProxyServer.getInstance();
            PluginManager pm = server.getPluginManager();
            
            if(tag.endsWith("bungee-console") || tag.endsWith("bc")) {
                CommandSender console = server.getConsole();
                pm.dispatchCommand(console, line);
                return;
            }
            
            if(tag.endsWith("bungee-player") || tag.endsWith("bp")) {
                Connection connection = e.getReceiver();
                String playerName = connection.toString();
                
                ProxiedPlayer player = server.getPlayer(playerName);
                pm.dispatchCommand(player, line);
                return;
            }
        } catch(Exception ex) {
            Logger logger = getLogger();
            logger.log(Level.SEVERE, "An error occurred while processing a command!", ex);
        }
    }
}