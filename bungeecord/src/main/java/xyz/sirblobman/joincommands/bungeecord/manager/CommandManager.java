package xyz.sirblobman.joincommands.bungeecord.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.md_5.bungee.config.Configuration;

import xyz.sirblobman.joincommands.bungeecord.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.bungeecord.command.ProxyJoinCommand;

public final class CommandManager {
    private final JoinCommandsPlugin plugin;
    private final List<ProxyJoinCommand> proxyJoinCommandList;

    public CommandManager(@NotNull JoinCommandsPlugin plugin) {
        this.plugin = plugin;
        this.proxyJoinCommandList = new ArrayList<>();
    }

    private @NotNull JoinCommandsPlugin getPlugin() {
        return this.plugin;
    }

    private @NotNull Logger getLogger() {
        JoinCommandsPlugin plugin = getPlugin();
        return plugin.getLogger();
    }


    public @NotNull List<ProxyJoinCommand> getProxyJoinCommandList() {
        return Collections.unmodifiableList(this.proxyJoinCommandList);
    }

    public void loadProxyJoinCommands() {
        this.proxyJoinCommandList.clear();

        JoinCommandsPlugin plugin = getPlugin();
        Configuration configuration = plugin.getConfig();
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

    private @Nullable ProxyJoinCommand loadProxyJoinCommand(@NotNull String commandId, @NotNull Configuration section) {
        List<String> commandList = section.getStringList("command-list");
        String permission = section.getString("permission");
        boolean firstJoinOnly = section.getBoolean("first-join-only");
        long delay = section.getLong("delay");

        try {
            ProxyJoinCommand command = new ProxyJoinCommand(commandId);
            command.setCommandList(commandList);
            command.setPermissionName(permission);
            command.setFirstJoinOnly(firstJoinOnly);
            command.setDelay(delay);
            return command;
        } catch (IllegalArgumentException ex) {
            Logger logger = getLogger();
            String messageFormat = "Failed to load a proxy join command with id '%s':";
            logger.log(Level.WARNING, String.format(Locale.US, messageFormat, commandId), ex);
            return null;
        }
    }
}
