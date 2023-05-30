package xyz.sirblobman.joincommands.bungeecord.listener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import xyz.sirblobman.joincommands.bungeecord.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.bungeecord.manager.CommandManager;
import xyz.sirblobman.joincommands.bungeecord.object.ProxyJoinCommand;

public final class ListenerJoinCommands implements Listener {
    private final JoinCommandsPlugin plugin;

    public ListenerJoinCommands(@NotNull JoinCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(PluginMessageEvent e) {
        Connection connection = e.getReceiver();
        if (!(connection instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) connection;
        String channel = e.getTag();
        byte[] data = e.getData();

        if (channel.equals("jc:player")) {
            e.setCancelled(true);
            runPlayerCommand(player, data);
            return;
        }

        if (channel.equals("jc:console")) {
            e.setCancelled(true);
            runConsoleCommand(data);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        runProxyJoinCommands(player);
        setJoinedProxyBefore(player);
    }

    private @NotNull JoinCommandsPlugin getPlugin() {
        return this.plugin;
    }

    private @NotNull Logger getLogger() {
        JoinCommandsPlugin plugin = getPlugin();
        return plugin.getLogger();
    }

    private @NotNull ProxyServer getProxy() {
        JoinCommandsPlugin plugin = getPlugin();
        return plugin.getProxy();
    }

    private void runPlayerCommand(@NotNull ProxiedPlayer player, byte @NotNull [] data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            DataInputStream dataStream = new DataInputStream(inputStream);
            String command = dataStream.readUTF();

            ProxyServer proxy = getProxy();
            PluginManager manager = proxy.getPluginManager();
            manager.dispatchCommand(player, command);
        } catch (IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.WARNING, "Failed to parse a command from channel 'jc:player':", ex);
        }
    }

    private void runConsoleCommand(byte @NotNull [] data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            DataInputStream dataStream = new DataInputStream(inputStream);
            String command = dataStream.readUTF();

            ProxyServer proxy = getProxy();
            CommandSender console = proxy.getConsole();

            PluginManager manager = proxy.getPluginManager();
            manager.dispatchCommand(console, command);
        } catch (IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.WARNING, "Failed to parse a command from channel 'jc:console':", ex);
        }
    }

    private void runProxyJoinCommands(@NotNull ProxiedPlayer player) {
        JoinCommandsPlugin plugin = getPlugin();
        CommandManager commandManager = plugin.getCommandManager();
        List<ProxyJoinCommand> commandList = commandManager.getProxyJoinCommandList();

        ProxyServer proxy = plugin.getProxy();
        TaskScheduler scheduler = proxy.getScheduler();

        for (ProxyJoinCommand command : commandList) {
            if (!command.canExecute(plugin, player)) {
                continue;
            }

            long delay = command.getDelay();
            Runnable task = () -> command.execute(plugin, player);
            scheduler.schedule(plugin, task, delay, TimeUnit.SECONDS);
        }
    }

    private void setJoinedProxyBefore(@NotNull ProxiedPlayer player) {
        JoinCommandsPlugin plugin = getPlugin();
        Configuration configuration = plugin.getConfig();
        if (configuration.getBoolean("disable-player-data", false)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String playerIdString = playerId.toString();
        String path = ("joined-before." + playerIdString);

        configuration.set(path, true);
        plugin.saveConfig();
    }
}
