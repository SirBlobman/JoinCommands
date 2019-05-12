package com.SirBlobman.joincommands.bukkit.config;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.SirBlobman.joincommands.bukkit.JoinCommands;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SQLiteUtil {
    private static final String DATABASE_FILE_NAME = "./plugins/JoinCommands/database.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_FILE_NAME;
    
    private static void log(String message) {
        Plugin plugin = JavaPlugin.getPlugin(JoinCommands.class);
        Logger logger = plugin.getLogger();
        logger.info(message);
    }
    
    private static Connection connection;
    public static Connection connectToDatabase() {
        try {
            if(connection != null && !connection.isClosed()) return connection;
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            if(connection == null) {
                log("Failed to create/load database file '" + DATABASE_FILE_NAME + "'.");
                return null;
            }
            
            DatabaseMetaData meta = connection.getMetaData();
            String driverName = meta.getDriverName();
            String driverVersion = meta.getDriverVersion();
            log("Successfully connected to '" + DATABASE_URL + "' using '" + driverName + " v" + driverVersion + "'.");
            
            String createTableCommand = "CREATE TABLE IF NOT EXISTS `joincommands` (`UUID` PRIMARY KEY, `Joined Worlds`);";
            PreparedStatement statement = connection.prepareStatement(createTableCommand);
            statement.execute();
            statement.close();
            
            return connection;
        } catch(ClassNotFoundException ex) {
            log("Could not find SQLite API.");
            ex.printStackTrace();
            return null;
        } catch(SQLException ex) {
            log("An SQL error occurred!");
            ex.printStackTrace();
            return null;
        }
    }
    
    public static List<String> getJoinedWorlds(OfflinePlayer player) throws SQLException {
        if(player == null) return new ArrayList<>();
        
        UUID uuid = player.getUniqueId();
        String uuidString = uuid.toString();
        
        connection = connectToDatabase();
        
        String sqlCommand = "SELECT `Joined Worlds` FROM `joincommands` WHERE `UUID`=? ;";
        PreparedStatement statement = connection.prepareStatement(sqlCommand);
        statement.setString(1, uuidString);
        
        ResultSet results = statement.executeQuery();
        if(!results.next()) {
            results.close();
            return new ArrayList<>();
        }
        
        String worldNameListString = results.getString("Joined Worlds");
        String[] worldNameListSplit = worldNameListString.split(Pattern.quote("|"));
        List<String> worldNameList = new ArrayList<>(Arrays.asList(worldNameListSplit));
        
        results.close();
        return worldNameList;
    }
    
    public static boolean hasJoinedWorld(OfflinePlayer player, String worldName) {
        if(player == null || worldName == null) return false;
        
        try {
            List<String> worldNameList = getJoinedWorlds(player);
            return worldNameList.contains(worldName);
        } catch(SQLException ex) {
            log("An SQL error occurred!");
            ex.printStackTrace();
            return false;
        }
    }
    
    public static void setJoinedWorlds(OfflinePlayer player, List<String> worldNameList) throws SQLException {
        if(player == null || worldNameList == null) return;
        
        UUID uuid = player.getUniqueId();
        String uuidString = uuid.toString();
        
        List<String> worldNameListNoDuplicates = worldNameList.stream().distinct().collect(Collectors.toList());
        String worldNameListString = String.join("|", worldNameListNoDuplicates);
        
        connection = connectToDatabase();
        String sqlCommand1 = "SELECT `Joined Worlds` FROM `joincommands` WHERE `UUID`=? ;";
        PreparedStatement statement1 = connection.prepareStatement(sqlCommand1);
        statement1.setString(1, uuidString);
        
        ResultSet results1 = statement1.executeQuery();
        if(!results1.next()) {
            String sqlCommand2 = "INSERT INTO `joincommands` (`UUID`, `Joined Worlds`) VALUES(?, ?) ;";
            PreparedStatement statement2 = connection.prepareStatement(sqlCommand2);
            statement2.setString(1, uuidString);
            statement2.setString(2, worldNameListString);
            statement2.executeUpdate();
            statement2.close();
        } else {
            String sqlCommand3 = "UPDATE `joincommands` SET `Joined Worlds`=? WHERE `UUID`=? ;";
            PreparedStatement statement3 = connection.prepareStatement(sqlCommand3);
            statement3.setString(1, worldNameListString);
            statement3.setString(2, uuidString);
            statement3.executeUpdate();
            statement3.close();
        }
        
        results1.close();
        statement1.close();
    }
    
    public static void addJoinedWorld(OfflinePlayer player, String worldName) {
        if(player == null || worldName == null || worldName.isEmpty()) return;
        
        try {
            List<String> worldNameList = getJoinedWorlds(player);
            worldNameList.add(worldName);
            setJoinedWorlds(player, worldNameList);
        } catch(SQLException ex) {
            log("An SQL error has occurred!");
            ex.printStackTrace();
            return;
        }
    }
}