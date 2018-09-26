package com.SirBlobman.joincommands.config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;

import com.SirBlobman.joincommands.JoinCommands;
import com.google.common.collect.Lists;

public class SQLiteData {
    private static final String DATABASE_FILE_NAME = "./players.db";
    private static final String URL = "jdbc:sqlite:" + DATABASE_FILE_NAME;
    private static Connection CONNECTION;
    public static Connection connectToDatabase() {
        try {
            if(CONNECTION == null || CONNECTION.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                CONNECTION = DriverManager.getConnection(URL);
                if(CONNECTION != null) {
                    DatabaseMetaData dmd = CONNECTION.getMetaData();
                    String driverName = dmd.getDriverName();
                    String driverVersion = dmd.getDriverVersion();
                    String msg = "Successfully connected to 'players.db' using '" + driverName + " v" + driverVersion + "'.";
                    JoinCommands.log(msg);
                    
                    String createTableCommand = "CREATE TABLE IF NOT EXISTS `joincommands` (`UUID` PRIMARY KEY, `Joined Worlds`);";
                    PreparedStatement ps = CONNECTION.prepareStatement(createTableCommand);
                    ps.execute();
                    ps.close();
                    
                    return CONNECTION;
                } else {
                    String error = "Failed to connect to database.sqlite";
                    JoinCommands.log(error);
                    return null;
                }
            } else return CONNECTION;
        } catch(Throwable ex) {
            String error = "Failed to connect to SQLite Database using URL: '" + URL + "'";
            JoinCommands.log(error);
            ex.printStackTrace();
            return null;
        }
    }
    
    public static void addJoinedWorld(OfflinePlayer op, String worldName) {
        Validate.notNull(op, "op cannot be NULL!");
        Validate.notEmpty(worldName, "worldName cannot be empty or NULL!");
        
        List<String> worldNames = getJoinedWorldNames(op);
        worldNames.add(worldName);
        setJoinedWorldNames(op, worldNames);
    }
    
    public static void setJoinedWorldNames(OfflinePlayer op, List<String> worldNames) {
        Validate.notNull(op, "op cannot be NULL!");
        Validate.notEmpty(worldNames, "worldNames cannot be empty or NULL!");
        
        try {
            UUID uuid = op.getUniqueId();
            final String uuidString = uuid.toString();

            worldNames = worldNames.stream().distinct().collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < worldNames.size(); i++) {
                String worldName = worldNames.get(i);
                if(i != 0) sb.append("|");
                sb.append(worldName);
            }
            final String worldsString = sb.toString();
            
            Connection conn = connectToDatabase();
            String sqlCommand0 = "SELECT * FROM `joincommands` WHERE `UUID`=?";
            PreparedStatement ps0 = conn.prepareStatement(sqlCommand0);
            ps0.setString(1, uuidString);
            ResultSet rs0 = ps0.executeQuery();
            if(rs0.first()) {
                String oldWorldNamesString = rs0.getString("Joined Worlds");
                List<String> oldWorldNames = Lists.newArrayList(oldWorldNamesString.split("|"));
                worldNames.addAll(oldWorldNames);
                worldNames = worldNames.stream().distinct().collect(Collectors.toList());
                
                StringBuilder sb1 = new StringBuilder();
                for(int i = 0; i < worldNames.size(); i++) {
                    String worldName = worldNames.get(i);
                    if(i != 0) sb1.append("|");
                    sb1.append(worldName);
                }
                final String newWorldsString = sb1.toString();
                
                String sqlCommand1 = "UPDATE `joincommands` SET `Joined Worlds`=? WHERE `UUID`=? LIMIT 1;";
                PreparedStatement ps1 = conn.prepareStatement(sqlCommand1);
                ps1.setString(1, newWorldsString);
                ps1.setString(2, uuidString);
                ps1.executeUpdate();
                ps1.close();
            } else {
                String sqlCommand1 = "INSERT INTO `joincommands` (`UUID`, `Joined Worlds`) VALUES(?, ?)";
                PreparedStatement ps1 = conn.prepareStatement(sqlCommand1);
                ps1.setString(1, uuidString);
                ps1.setString(2, worldsString);
                ps1.executeUpdate();
                ps1.close();
            }
            
            ps0.close();
            rs0.close();
        } catch(Throwable ex) {
            String error = "An error occured while changing player data in 'players.db'";
            JoinCommands.log(error);
            ex.printStackTrace();
            return;
        }
    }
    
    public static List<String> getJoinedWorldNames(OfflinePlayer op) {
        Validate.notNull(op, "op cannot be NULL!");
        try {
            UUID uuid = op.getUniqueId();
            final String uuidString = uuid.toString();
            
            Connection conn = connectToDatabase();
            String sqlCommand0 = "SELECT * FROM `joincommands` WHERE `UUID`=?";
            PreparedStatement ps0 = conn.prepareStatement(sqlCommand0);
            ps0.setString(1, uuidString);
            ResultSet rs0 = ps0.executeQuery();
            if(rs0.first()) {
                String worldNamesString = rs0.getString("Joined Worlds");
                List<String> worldNames = Lists.newArrayList(worldNamesString.split("|"));
                worldNames = worldNames.stream().distinct().collect(Collectors.toList());
                
                rs0.close();
                return worldNames;
            } else {
                rs0.close();
                return new ArrayList<>();
            }
        } catch(Throwable ex) {
            String error = "An error occured while getting player data in 'players.db'";
            JoinCommands.log(error);
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public static boolean hasJoinedWorld(OfflinePlayer op, String worldName) {
        Validate.notNull(op, "op cannot be null!");
        List<String> worldNames = getJoinedWorldNames(op);
        return worldNames.contains(worldName);
    }
}