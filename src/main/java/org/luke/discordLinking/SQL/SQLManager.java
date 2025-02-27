package org.luke.discordLinking.SQL;

import org.luke.discordLinking.Data;
import org.luke.discordLinking.MyCallBack;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLManager {

    static Connection connection = null;
    static final String tableName = "linked_data";
    final static String column_uuid = "minecraft_uuid";
    static final String column_discordID = "discord_id";
    static final String column_linked_data = "link_data";

    public static void ConnectionToDatabase() {
        String url = "jdbc:mysql://" + Data.mysqlURL;
        try {
            // MySQLドライバのロード
            Class.forName("com.mysql.cj.jdbc.Driver");
            // データベースへの接続
            System.out.println(Data.mysqlUserName + "   " + Data.mysqlPassword);
            connection = DriverManager.getConnection(url, Data.mysqlUserName, Data.mysqlPassword);
        } catch (Exception ignored) {

        }
    }

    public static Connection getConnection() {
        try {
            if(connection == null || connection.isClosed()) {
                ConnectionToDatabase();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static Statement getStatement() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("USE " + Data.mysqlDatabaseName);
            return stmt;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void CreateDatabase() {
        final String dbName = Data.mysqlDatabaseName;
        List<String> executes = new ArrayList<>();

        try (Statement statement = getConnection().createStatement()) {
            // データベース作成を実行
            executes.add("CREATE DATABASE IF NOT EXISTS " + dbName);

            executes.add("USE " + dbName);

            String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                    column_discordID + " VARCHAR(60) NOT NULL," +
                    column_linked_data + " JSON NOT NULL, " +
                    " PRIMARY KEY ( " + column_discordID + " )" +
                    " );";

            executes.add(createTable);
            //<---------------------------------->
            System.out.println("テーブル " + tableName + " が作成されました。");

            for (String execute : executes) {
                statement.executeUpdate(execute);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Utility class
    public static boolean isLinkedUser(UUID uuid) {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            statement.executeUpdate("USE " + Data.mysqlDatabaseName);
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            if (resultSet.next()) {
                UUID mc_uuid = UUID.fromString(resultSet.getString(column_uuid));
                if (mc_uuid.equals(uuid)) {
                    return true;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static UUID getUUIDFromDiscordID(Long discordID) {
        Statement statement = null;
        try {
            Statement statement = getStatement();
            String query = "SELECT * FROM " + tableName;
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Long discordDataID = resultSet.getLong(column_discordID);
                if (discordDataID.equals(discordID)) {
                    return UUID.fromString(resultSet.getString(column_uuid));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void removeLinkData(UUID uuid) {
        try {
            Statement statement = getStatement();

            statement.executeUpdate("USE " + Data.mysqlDatabaseName);
            statement.executeUpdate("DELETE FROM " + tableName + " WHERE `" + tableName + "`.`" + column_uuid + "` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
