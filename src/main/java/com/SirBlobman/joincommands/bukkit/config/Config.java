package com.SirBlobman.joincommands.bukkit.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.SirBlobman.joincommands.bukkit.JoinCommands;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Config {
    private static File FOLDER;
    private static File FILE;
    private static YamlConfiguration config;
    
    public static void load() {
        try {
            if(FOLDER == null || FILE == null) {
                FOLDER = JoinCommands.INSTANCE.getDataFolder();
                FILE = new File(FOLDER, "config.yml");
                if(!FOLDER.exists()) FOLDER.mkdirs();
            }
            
            if(!FILE.exists()) {
                JoinCommands.debug("Creating config file 'config.yml'.");
                Class<?> class_JoinCommands = JoinCommands.class;
                InputStream file_config_yml = class_JoinCommands.getResourceAsStream("/config.yml");
                if(file_config_yml != null) {
                    Path path = Paths.get(FILE.getPath());
                    Files.copy(file_config_yml, path, StandardCopyOption.REPLACE_EXISTING);
                }
                
                JoinCommands.debug("'config.yml' not found in jar, creating empty config...");
                FILE.createNewFile();
            }
            
            config = new YamlConfiguration();
            config.load(FILE);
        } catch(Exception ex) {
            JoinCommands.INSTANCE.getLogger().warning("Failed to load config 'config.yml'.");
            ex.printStackTrace();
        }
    }
    
    private static final List<JoinCommand> COMMAND_CACHE = new ArrayList<>();
    private static final List<WorldJoinCommand> WORLD_COMMAND_CACHE = new ArrayList<>();
    
    public static List<JoinCommand> getJoinCommands(boolean reload) {
        if(!COMMAND_CACHE.isEmpty() && !reload) return new ArrayList<>(COMMAND_CACHE);

        load();
        COMMAND_CACHE.clear();
        if(!config.isConfigurationSection("join commands")) {
            JoinCommands.INSTANCE.getLogger().warning("Invalid `config.yml`: Missing section 'join commands'.");
            return new ArrayList<>();
        }
        
        ConfigurationSection commandListSection = config.getConfigurationSection("join commands");
        Set<String> joinCommandIdList = commandListSection.getKeys(false);
        if(joinCommandIdList == null || joinCommandIdList.isEmpty()) return new ArrayList<>();
        
        for(String joinCommandId : joinCommandIdList) {
            if(!commandListSection.isConfigurationSection(joinCommandId)) {
                JoinCommands.INSTANCE.getLogger().warning("Found invalid join command '" + joinCommandId + "'. Please remove it or fix it.");
                continue;
            }
            
            ConfigurationSection commandSection = commandListSection.getConfigurationSection(joinCommandId);
            
            if(!commandSection.isString("command")) {
                JoinCommands.INSTANCE.getLogger().warning("Invalid or missing 'command' for join command '" + joinCommandId + "'.");
                continue;
            }
            String command = commandSection.getString("command");
            
            if(!commandSection.isInt("delay")) {
                JoinCommands.INSTANCE.getLogger().warning("Invalid or missing 'delay' for join command '" + joinCommandId + "'.");
                continue;
            }
            int delay = commandSection.getInt("delay");
            
            String permission = commandSection.isString("permission") ? commandSection.getString("permission") : "";
            
            if(!commandSection.isBoolean("first join only")) {
                JoinCommands.INSTANCE.getLogger().warning("Invalid or missing 'first join only' for join command '" + joinCommandId + "'.");
                continue;
            }
            boolean firstJoinOnly = commandSection.getBoolean("first join only");
            
            JoinCommand joinCommand = new JoinCommand(command, delay, permission, firstJoinOnly);
            COMMAND_CACHE.add(joinCommand);
        }
        
        return new ArrayList<>(COMMAND_CACHE);
    }
    
    public static List<WorldJoinCommand> getWorldJoinCommands(boolean reload) {
        if(!WORLD_COMMAND_CACHE.isEmpty() && !reload) return new ArrayList<>(WORLD_COMMAND_CACHE);

        load();
        WORLD_COMMAND_CACHE.clear();
        if(!config.isConfigurationSection("world commands")) {
            JoinCommands.INSTANCE.getLogger().warning("Invalid `config.yml`: Missing section 'world commands'.");
            return new ArrayList<>();
        }
        
        ConfigurationSection commandListSection = config.getConfigurationSection("world commands");
        Set<String> worldCommandIdList = commandListSection.getKeys(false);
        if(worldCommandIdList == null || worldCommandIdList.isEmpty()) return new ArrayList<>();
        
        for(String worldCommandId : worldCommandIdList) {
            if(!commandListSection.isConfigurationSection(worldCommandId)) {
                JoinCommands.INSTANCE.getLogger().warning("Found invalid world command '" + worldCommandId + "'. Please remove it or fix it.");
                continue;
            }
            
            ConfigurationSection commandSection = commandListSection.getConfigurationSection(worldCommandId);
            
            if(!commandSection.isString("command")) {
                JoinCommands.INSTANCE.getLogger().warning("Invalid or missing 'command' for world command '" + worldCommandId + "'.");
                continue;
            }
            String command = commandSection.getString("command");
            
            if(!commandSection.isInt("delay")) {
                JoinCommands.INSTANCE.getLogger().warning("Invalid or missing 'delay' for world command '" + worldCommandId + "'.");
                continue;
            }
            int delay = commandSection.getInt("delay");
            
            String permission = commandSection.isString("permission") ? commandSection.getString("permission") : "";
            
            if(!commandSection.isBoolean("first join only")) {
                JoinCommands.INSTANCE.getLogger().warning("Invalid or missing 'first join only' for world command '" + worldCommandId + "'.");
                continue;
            }
            boolean firstJoinOnly = commandSection.getBoolean("first join only");
            
            if(!commandSection.isList("worlds")) {
                JoinCommands.INSTANCE.getLogger().warning("Invalid or missing 'worlds' for world command '" + worldCommandId + "'.");
                continue;
            }
            List<String> worldList = commandSection.getStringList("worlds");
            
            WorldJoinCommand worldCommand = new WorldJoinCommand(worldList, command, delay, permission, firstJoinOnly);
            WORLD_COMMAND_CACHE.add(worldCommand);
        }
        
        return new ArrayList<>(WORLD_COMMAND_CACHE);
    }
}
