package xyz.sirblobman.joincommands.velocity.configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import xyz.sirblobman.joincommands.common.utility.Validate;
import xyz.sirblobman.joincommands.velocity.JoinCommandsPlugin;

import com.google.common.reflect.TypeToken;
import com.velocitypowered.api.proxy.Player;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader.Builder;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

public final class PlayerDataConfiguration {
    private final JoinCommandsPlugin plugin;
    private final YAMLConfigurationLoader configurationLoader;

    private ConfigurationNode configuration;

    private final Set<UUID> joinedBeforeSet;

    public PlayerDataConfiguration(JoinCommandsPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");

        Path dataDirectory = plugin.getDataDirectory();
        Path configPath = dataDirectory.resolve("playerdata.yml");

        Builder builder = YAMLConfigurationLoader.builder();
        builder.setIndent(2);
        builder.setFlowStyle(FlowStyle.AUTO);
        builder.setPath(configPath);
        this.configurationLoader = builder.build();

        this.joinedBeforeSet = new HashSet<>();
    }

    @SuppressWarnings("UnstableApiUsage")
    public void load() {
        try {
            this.configuration = this.configurationLoader.load();
            ConfigurationNode node = this.configuration.getNode("joined-before-list");

            TypeToken<UUID> stringToken = TypeToken.of(UUID.class);
            List<UUID> copyList = node.getList(stringToken);

            this.joinedBeforeSet.clear();
            this.joinedBeforeSet.addAll(copyList);
        } catch(IOException | ObjectMappingException ex) {
            Logger logger = this.plugin.getLogger();
            logger.log(Level.SEVERE, "Failed to load the player data configuration:", ex);
        }
    }

    public void save() {
        try {
            List<UUID> copyList = new ArrayList<>(this.joinedBeforeSet);
            ConfigurationNode node = this.configuration.getNode("joined-before-list");
            node.setValue(copyList);

            this.configurationLoader.save(this.configuration);
        } catch(IOException ex) {
            Logger logger = this.plugin.getLogger();
            logger.log(Level.SEVERE, "Failed to save the player data configuration:", ex);
        }
    }

    public boolean hasPlayerJoinedBefore(Player player) {
        UUID playerId = player.getUniqueId();
        return this.joinedBeforeSet.contains(playerId);
    }

    public void setJoined(Player player) {
        UUID playerId = player.getUniqueId();
        this.joinedBeforeSet.add(playerId);
        save();
    }
}
