package xyz.sirblobman.joincommands.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import xyz.sirblobman.joincommands.common.utility.Validate;
import xyz.sirblobman.joincommands.velocity.configuration.PlayerDataConfiguration;
import xyz.sirblobman.joincommands.velocity.configuration.VelocityConfiguration;
import xyz.sirblobman.joincommands.velocity.listener.ListenerJoinCommands;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader.Builder;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

public final class JoinCommandsPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private final VelocityConfiguration configuration;
    private final PlayerDataConfiguration playerData;

    @Inject
    public JoinCommandsPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = Validate.notNull(server, "server must not be null!");
        this.logger = Validate.notNull(logger, "logger must not be null!");
        this.dataDirectory = Validate.notNull(dataDirectory, "dataDirectory must not be null!");

        this.configuration = new VelocityConfiguration(this.logger);
        this.playerData = new PlayerDataConfiguration(this);

        saveDefaultConfig();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        reloadConfig();

        ProxyServer server = getServer();
        ChannelRegistrar channelRegistrar = server.getChannelRegistrar();
        ChannelIdentifier consoleChannel = MinecraftChannelIdentifier.create("jc", "console");
        ChannelIdentifier playerChannel = MinecraftChannelIdentifier.create("jc", "player");
        channelRegistrar.register(consoleChannel, playerChannel);

        EventManager eventManager = server.getEventManager();
        eventManager.register(this, new ListenerJoinCommands(this));
    }

    public ProxyServer getServer() {
        return this.server;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Path getDataDirectory() {
        return this.dataDirectory;
    }

    public VelocityConfiguration getConfiguration() {
        return this.configuration;
    }

    public PlayerDataConfiguration getPlayerData() {
        return this.playerData;
    }

    private void saveDefaultConfig() {
        try {
            Path dataDirectory = getDataDirectory();
            if (Files.notExists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve("config.yml");
            if (Files.exists(configFile)) {
                return;
            }

            Class<?> thisClass = getClass();
            InputStream jarConfigStream = thisClass.getResourceAsStream("/config-velocity.yml");
            if (jarConfigStream == null) {
                throw new IOException("Missing file 'config-velocity.yml' in jar.");
            }

            Files.copy(jarConfigStream, configFile, StandardCopyOption.REPLACE_EXISTING);
            jarConfigStream.close();
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.SEVERE, "An error occurred while saving the default configuration.", ex);
        }
    }

    private void reloadConfig() {
        try {
            Path dataDirectory = getDataDirectory();
            Path configFile = dataDirectory.resolve("config.yml");
            if (Files.notExists(configFile) || !Files.isRegularFile(configFile)) {
                throw new IOException("The 'config.yml' file does not exist.");
            }

            Builder builder = YAMLConfigurationLoader.builder();
            builder.setIndent(2);
            builder.setFlowStyle(FlowStyle.AUTO);
            builder.setPath(configFile);

            YAMLConfigurationLoader loader = builder.build();
            ConfigurationNode configurationNode = loader.load();

            VelocityConfiguration configuration = getConfiguration();
            configuration.load(configurationNode);

            if (!configuration.isDisablePlayerData()) {
                Path playerDataPath = dataDirectory.resolve("playerdata.yml");
                if (Files.notExists(playerDataPath)) {
                    Files.createFile(playerDataPath);
                }

                PlayerDataConfiguration playerData = getPlayerData();
                playerData.load();
            }
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.SEVERE, "An error occurred while reloading the configurations.", ex);
        }
    }
}
