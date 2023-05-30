package xyz.sirblobman.joincommands.velocity.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import xyz.sirblobman.joincommands.velocity.object.ProxyJoinCommand;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public final class VelocityConfiguration {
    private final Logger logger;
    private final List<ProxyJoinCommand> proxyJoinCommandList;
    private boolean debugMode;
    private boolean disablePlayerData;

    public VelocityConfiguration(@NotNull Logger logger) {
        this.logger = logger;
        this.debugMode = false;
        this.disablePlayerData = false;
        this.proxyJoinCommandList = new ArrayList<>();
    }

    private @NotNull Logger getLogger() {
        return this.logger;
    }

    public void load(@NotNull ConfigurationNode configurationNode) {
        ConfigurationNode debugModeNode = configurationNode.getNode("debug-mode");
        setDebugMode(debugModeNode.getBoolean());

        ConfigurationNode disablePlayerDataNode = configurationNode.getNode("disable-player-data");
        setDisablePlayerData(disablePlayerDataNode.getBoolean());

        ConfigurationNode proxyJoinCommandsNode = configurationNode.getNode("proxy-join-commands");
        List<? extends ConfigurationNode> proxyJoinCommandNodes = proxyJoinCommandsNode.getChildrenList();
        loadProxyJoinCommands(proxyJoinCommandNodes);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isDisablePlayerData() {
        return disablePlayerData;
    }

    public void setDisablePlayerData(boolean disablePlayerData) {
        this.disablePlayerData = disablePlayerData;
    }

    public @NotNull List<ProxyJoinCommand> getProxyJoinCommandList() {
        return Collections.unmodifiableList(this.proxyJoinCommandList);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void loadProxyJoinCommands(@NotNull List<? extends ConfigurationNode> nodes) {
        this.proxyJoinCommandList.clear();

        for (ConfigurationNode node : nodes) {
            ConfigurationNode permissionNode = node.getNode("permission");
            ConfigurationNode firstJoinOnlyNode = node.getNode("first-join-only");
            ConfigurationNode delayNode = node.getNode("delay");
            ConfigurationNode commandListNode = node.getNode("command-list");

            String permission = permissionNode.getString();
            boolean firstJoinOnly = firstJoinOnlyNode.getBoolean();
            long delay = delayNode.getLong();

            List<String> commandList;
            try {
                TypeToken<String> stringToken = TypeToken.of(String.class);
                commandList = commandListNode.getList(stringToken);
            } catch (ObjectMappingException ex) {
                Logger logger = getLogger();
                logger.log(Level.WARNING, "Failed to load command list from node '" + node.getKey() + "':", ex);
                commandList = new ArrayList<>();
            }

            ProxyJoinCommand proxyJoinCommand = new ProxyJoinCommand(commandList, permission, firstJoinOnly, delay);
            this.proxyJoinCommandList.add(proxyJoinCommand);
        }
    }
}
