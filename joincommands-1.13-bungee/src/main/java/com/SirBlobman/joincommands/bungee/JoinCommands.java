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

public class JoinCommands extends Plugin implements Listener {
    public static final Logger LOG = Logger.getLogger("Join Commands");
	public static ProxyServer SERVER = ProxyServer.getInstance();
	public static CommandSender CONSOLE = SERVER.getConsole();
	public static PluginManager PM = SERVER.getPluginManager();

	@Override
	public void onEnable() {
		SERVER.registerChannel("joincommands:bungeecord");
		PM.registerListener(this, this);
	}

	@EventHandler
	public void onReceive(PluginMessageEvent e) {
		String tag = e.getTag();
		if(tag.equals("joincommands:bungeecord")) {
			try {
				byte[] data = e.getData();
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				DataInputStream in = new DataInputStream(bais);

				String line = in.readUTF();
				LOG.info("[Channel BC-JoinCommands] Incoming Message: " + line);
				if(line.startsWith("CJC:")) {
					String cmd = line.substring(4);
					PM.dispatchCommand(CONSOLE, cmd);
				} else if(line.startsWith("PJC:")) {
					Connection rec = e.getReceiver();
					String name = rec.toString();
					ProxiedPlayer pp = SERVER.getPlayer(name);

					String cmd = line.substring(4);
					PM.dispatchCommand(pp, cmd);
				}
			} catch(Throwable ex) {
				String error = "There was an error processing the command!";
				LOG.log(Level.SEVERE, error, ex);
			}
		}
	}
}