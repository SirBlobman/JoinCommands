package xyz.sirblobman.joincommands.spigot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import xyz.sirblobman.joincommands.spigot.command.CommandJoinCommands;
import xyz.sirblobman.joincommands.spigot.listener.ListenerJoinCommands;
import xyz.sirblobman.joincommands.spigot.manager.CommandManager;
import xyz.sirblobman.joincommands.spigot.manager.PlayerDataManager;

public final class JoinCommandsPlugin extends JavaPlugin {
    private final PlayerDataManager playerDataManager;
    private final CommandManager commandManager;

    public JoinCommandsPlugin() {
        this.playerDataManager = new PlayerDataManager(this);
        this.commandManager = new CommandManager(this);
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        reloadConfig();

        registerBungeeCordChannels();
        registerListener();

        new CommandJoinCommands(this).register();
    }

    @Override
    public void onDisable() {
        // Do Nothing
    }

    @Override
    public void saveDefaultConfig() {
        try {
            File dataFolder = getDataFolder();
            Path dataDirectory = dataFolder.toPath();
            if (Files.notExists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve("config.yml");
            if (Files.exists(configFile)) {
                return;
            }

            Class<?> thisClass = getClass();
            InputStream jarConfigStream = thisClass.getResourceAsStream("/config-spigot.yml");
            if (jarConfigStream == null) {
                throw new IOException("Missing file 'config-spigot.yml' in jar.");
            }

            Files.copy(jarConfigStream, configFile, StandardCopyOption.REPLACE_EXISTING);
            jarConfigStream.close();
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.SEVERE, "An error occurred while saving the default configuration.", ex);
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        CommandManager commandManager = getCommandManager();
        commandManager.loadServerJoinCommands();
        commandManager.loadWorldJoinCommands();
    }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public boolean usePlaceholderAPIHook() {
        FileConfiguration config = getConfig();
        boolean useHook = config.getBoolean("placeholderapi-hook", true);

        PluginManager pluginManager = Bukkit.getPluginManager();
        return (useHook && pluginManager.isPluginEnabled("PlaceholderAPI"));
    }

    private void registerBungeeCordChannels() {
        FileConfiguration config = getConfig();
        if (!config.getBoolean("bungeecord-hook")) {
            return;
        }

        Messenger messenger = Bukkit.getMessenger();
        messenger.registerOutgoingPluginChannel(this, "jc:console");
        messenger.registerOutgoingPluginChannel(this, "jc:player");
    }

    private void registerListener() {
        PluginManager manager = Bukkit.getPluginManager();
        Listener listener = new ListenerJoinCommands(this);
        manager.registerEvents(listener, this);
    }
}
