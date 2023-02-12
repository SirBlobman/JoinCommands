package xyz.sirblobman.joincommands.spigot.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import xyz.sirblobman.joincommands.spigot.JoinCommandsPlugin;

public final class CommandJoinCommands implements TabExecutor {
    private final JoinCommandsPlugin plugin;

    public CommandJoinCommands(JoinCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        String sub = args[0].toLowerCase();
        if (!sub.equals("reload")) {
            return false;
        }

        this.plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the JoinCommands configuration.");
        return true;
    }

    public void register() {
        PluginCommand pluginCommand = this.plugin.getCommand("join-commands");
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }
}
