package xyz.sirblobman.joincommands.velocity.listener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import xyz.sirblobman.joincommands.common.utility.Validate;
import xyz.sirblobman.joincommands.velocity.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.velocity.configuration.PlayerDataConfiguration;
import xyz.sirblobman.joincommands.velocity.configuration.VelocityConfiguration;
import xyz.sirblobman.joincommands.velocity.object.ProxyJoinCommand;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelMessageSink;
import com.velocitypowered.api.scheduler.Scheduler;
import com.velocitypowered.api.scheduler.Scheduler.TaskBuilder;

public final class ListenerJoinCommands {
    private final JoinCommandsPlugin plugin;

    public ListenerJoinCommands(JoinCommandsPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
    }

    private JoinCommandsPlugin getPlugin() {
        return this.plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        ChannelMessageSink connection = e.getTarget();
        if (!(connection instanceof Player)) {
            return;
        }

        Player player = (Player) connection;
        ChannelIdentifier identifier = e.getIdentifier();
        String channel = identifier.getId();
        byte[] data = e.getData();

        if (channel.equals("jc:player")) {
            e.setResult(ForwardResult.handled());
            runPlayerCommand(player, data);
            return;
        }

        if (channel.equals("jc:console")) {
            e.setResult(ForwardResult.handled());
            runConsoleCommand(data);
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent e) {
        Player player = e.getPlayer();
        runProxyJoinCommands(player);
        setJoinedProxyBefore(player);
    }

    private void runPlayerCommand(Player player, byte[] data) {
        Validate.notNull(player, "player must not be null!");
        Validate.notNull(data, "data must not be null!");
        JoinCommandsPlugin plugin = getPlugin();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            DataInputStream dataStream = new DataInputStream(inputStream);
            String command = dataStream.readUTF();

            ProxyServer proxy = plugin.getServer();
            CommandManager commandManager = proxy.getCommandManager();
            commandManager.executeAsync(player, command);
        } catch (IOException ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while parsing a jc:player command:", ex);
        }
    }

    private void runConsoleCommand(byte[] data) {
        Validate.notNull(data, "data must not be null!");
        JoinCommandsPlugin plugin = getPlugin();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            DataInputStream dataStream = new DataInputStream(inputStream);
            String command = dataStream.readUTF();

            ProxyServer proxy = plugin.getServer();
            ConsoleCommandSource console = proxy.getConsoleCommandSource();
            CommandManager commandManager = proxy.getCommandManager();
            commandManager.executeAsync(console, command);
        } catch (IOException ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while parsing a jc:console command:", ex);
        }
    }

    private void runProxyJoinCommands(Player player) {
        JoinCommandsPlugin plugin = getPlugin();
        VelocityConfiguration configuration = plugin.getConfiguration();
        List<ProxyJoinCommand> commandList = configuration.getProxyJoinCommandList();

        ProxyServer proxy = plugin.getServer();
        Scheduler scheduler = proxy.getScheduler();

        for (ProxyJoinCommand command : commandList) {
            if (!command.shouldBeExecutedFor(plugin, player)) {
                continue;
            }

            long delay = command.getDelay();
            Runnable task = () -> command.executeFor(plugin, player);
            TaskBuilder taskBuilder = scheduler.buildTask(plugin, task);

            Duration duration = Duration.of(delay, ChronoUnit.SECONDS);
            taskBuilder.delay(duration);
            taskBuilder.schedule();
        }
    }

    private void setJoinedProxyBefore(Player player) {
        JoinCommandsPlugin plugin = getPlugin();
        VelocityConfiguration configuration = plugin.getConfiguration();
        if (configuration.isDisablePlayerData()) {
            return;
        }

        PlayerDataConfiguration playerData = plugin.getPlayerData();
        playerData.setJoined(player);
    }
}
