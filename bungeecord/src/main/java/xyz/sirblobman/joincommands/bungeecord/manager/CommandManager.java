package xyz.sirblobman.joincommands.bungeecord.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.config.Configuration;

import xyz.sirblobman.joincommands.bungeecord.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.bungeecord.object.ProxyJoinCommand;

public final class CommandManager {
    private final JoinCommandsPlugin plugin;
    private final List<ProxyJoinCommand> proxyJoinCommandList;

    public CommandManager(JoinCommandsPlugin plugin) {
        this.plugin = plugin;
        this.proxyJoinCommandList = new ArrayList<>();
    }

    public JoinCommandsPlugin getPlugin() {
        return this.plugin;
    }

    public List<ProxyJoinCommand> getProxyJoinCommandList() {
        return Collections.unmodifiableList(this.proxyJoinCommandList);
    }

    public void loadProxyJoinCommands() {
        this.proxyJoinCommandList.clear();

        JoinCommandsPlugin plugin = getPlugin();
        Configuration configuration = plugin.getConfig();
        if (configuration == null) {
            return;
        }

        Configuration section = configuration.getSection("proxy-join-commands");
        if (section == null) {
            return;
        }

        Collection<String> commandIdList = section.getKeys();
        for (String commandId : commandIdList) {
            if (commandId == null || commandId.isEmpty()) {
                continue;
            }

            Configuration commandSection = section.getSection(commandId);
            if (commandSection == null) {
                continue;
            }

            ProxyJoinCommand proxyJoinCommand = loadProxyJoinCommand(commandId, commandSection);
            if (proxyJoinCommand == null) {
                continue;
            }

            this.proxyJoinCommandList.add(proxyJoinCommand);
        }
    }

    private ProxyJoinCommand loadProxyJoinCommand(String commandId, Configuration section) {
        if (section == null) {
            return null;
        }

        List<String> commandList = section.getStringList("command-list");
        String permission = section.getString("permission");
        boolean firstJoinOnly = section.getBoolean("first-join-only");
        long delay = section.getLong("delay");

        try {
            return new ProxyJoinCommand(commandList, permission, firstJoinOnly, delay);
        } catch (Exception ex) {
            JoinCommandsPlugin plugin = getPlugin();
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while loading the proxy join command with id '"
                    + commandId + "':", ex);
            return null;
        }
    }
}