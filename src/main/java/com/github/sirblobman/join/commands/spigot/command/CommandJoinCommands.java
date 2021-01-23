package com.github.sirblobman.join.commands.spigot.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.join.commands.spigot.JoinCommandsSpigot;

public final class CommandJoinCommands extends Command {
    private final JoinCommandsSpigot plugin;
    public CommandJoinCommands(JoinCommandsSpigot plugin) {
        super(plugin, "join-commands");
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return (args.length == 1 ? Collections.singletonList("reload") : Collections.emptyList());
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length < 1) return false;

        String sub = args[0].toLowerCase();
        if(!sub.equals("reload")) return false;

        this.plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the JoinCommands configuration.");
        return true;
    }
}