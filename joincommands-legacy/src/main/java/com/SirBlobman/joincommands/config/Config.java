package com.SirBlobman.joincommands.config;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.SirBlobman.joincommands.JoinCommands;
import com.google.common.collect.Lists;

public class Config {
    private static final File FOLDER = JoinCommands.FOLDER;
    private static final File FILE = new File(FOLDER, "config.yml");
    private static YamlConfiguration config = new YamlConfiguration();
    
    public static void load() {
        try {
            if(!FILE.exists()) copyFromJar("config.yml", FOLDER);
            config.load(FILE);
            getCommands(false);
        } catch(Throwable ex) {
            String error = "Failed to load config.yml!";
            JoinCommands.log(error);
        }
    }
    
    protected static void copyFromJar(String fileName, File folder) {
        try {
            InputStream is = JoinCommands.INSTANCE.getResource(fileName);
            File newFile = new File(folder, fileName);
            if(!folder.exists()) folder.mkdirs();
            if(!newFile.exists()) {
                if(is != null) {
                    FileUtils.copyInputStreamToFile(is, newFile);
                } else {
                    String error = "The file '" + fileName + "' does not exist in the jar.";
                    Bukkit.getConsoleSender().sendMessage(error);
                }
            } else {
                String error = "The file '" + fileName + "' already exists in '" + folder + "'.";
                Bukkit.getConsoleSender().sendMessage(error);
            }
        } catch(Throwable ex) {
            String error = "Failed to copy file '" + fileName + "' to '" + folder + "' from JAR:";
            Bukkit.getConsoleSender().sendMessage(error);
            ex.printStackTrace();
        }
    }
    
    public static List<JoinCommand> COMMAND_CACHE = Lists.newArrayList();
    public static List<WorldJoinCommand> WORLD_COMMAND_CACHE = Lists.newArrayList();
    public static List<JoinCommand> getCommands(boolean reload) {
        if(COMMAND_CACHE.isEmpty() || reload) {
            COMMAND_CACHE = Lists.newArrayList();
            if(reload) load();
            if(config.isConfigurationSection("join commands")) {
                ConfigurationSection cs = config.getConfigurationSection("join commands");
                for(String key : cs.getKeys(false)) {
                    if(cs.isConfigurationSection(key)) {
                        ConfigurationSection cs2 = cs.getConfigurationSection(key);
                        if(cs2.isString("command")) {
                            String command = cs2.getString("command");
                            if(cs2.isString("permission")) {
                                String permission = cs2.getString("permission");
                                if(cs2.isLong("delay") || cs2.isInt("delay")) {
                                    long delay = cs2.getLong("delay");
                                    if(cs2.isBoolean("first join only")) {
                                        boolean firstJoinOnly = cs2.getBoolean("first join only");
                                        JoinCommand jc = new JoinCommand(command, delay, permission, firstJoinOnly);
                                        COMMAND_CACHE.add(jc);
                                    } else {
                                        JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C6");
                                    }
                                } else {
                                    JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C5");
                                }
                            } else {
                                JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C4");
                            }
                        } else {
                            JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C3");
                        }
                    } else {
                        JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C2");
                    }
                }
            } else {
                JoinCommands.log("There was an error getting the join commands", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C1");
                return Lists.newArrayList();
            }
        }
        
        return COMMAND_CACHE;
    }
    
    public static List<WorldJoinCommand> getWorldCommands(boolean reload) {
        if(WORLD_COMMAND_CACHE.isEmpty() || reload) {
            WORLD_COMMAND_CACHE = Lists.newArrayList();
            if(reload) load();
            if(config.isConfigurationSection("world join commands")) {
                ConfigurationSection cs = config.getConfigurationSection("world join commands");
                for(String key : cs.getKeys(false)) {
                    if(cs.isConfigurationSection(key)) {
                        ConfigurationSection cs2 = cs.getConfigurationSection(key);
                        if(cs2.isList("worlds")) {
                            List<String> validWorlds = cs2.getStringList("worlds");
                            if(cs2.isString("command")) {
                                String command = cs2.getString("command");
                                if(cs2.isString("permission")) {
                                    String permission = cs2.getString("permission");
                                    if(cs2.isLong("delay") || cs2.isInt("delay")) {
                                        long delay = cs2.getLong("delay");
                                        if(cs2.isBoolean("first join only")) {
                                            boolean firstJoinOnly = cs2.getBoolean("first join only");
                                            WorldJoinCommand wjc = new WorldJoinCommand(validWorlds, command, delay, permission, firstJoinOnly);
                                            WORLD_COMMAND_CACHE.add(wjc);
                                        } else {
                                            JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C6");
                                        }
                                    } else {
                                        JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C5");
                                    }
                                } else {
                                    JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C4");
                                }
                            } else {
                                JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C3");
                            }
                        } else {
                            JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C7");
                        }
                    } else {
                        JoinCommands.log("Invalid command '" + key + "'", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C2");
                    }
                }
            } else {
                JoinCommands.log("There was an error getting the join commands", "If you don't how to fix this, PM the following error code to SirBlobman:", "Error Code: JC-C1");
                return Lists.newArrayList();
            }
        }
        
        return WORLD_COMMAND_CACHE;
    }
}