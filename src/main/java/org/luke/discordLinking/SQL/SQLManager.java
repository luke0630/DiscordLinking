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

    public static void CreateDatabase(MyCallBack.MyCallback callback) {
    public static void CreateDatabase() {
        final String dbName = Data.mysqlDatabaseName;
        List<String> executes = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();

            // データベース作成を実行
            executes.add("CREATE DATABASE IF NOT EXISTS " + dbName);

            executes.add("USE " + dbName);

            String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                    column_uuid + " VARCHAR(50) NOT NULL, " +
                    column_discordID + " VARCHAR(50) NOT NULL," +
                    column_creationDate + " VARCHAR(50) NOT NULL," +
                    " PRIMARY KEY ( " + column_uuid + " )" +
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
            statement = connection.createStatement();

            statement.executeUpdate("USE " + Data.mysqlDatabaseName);
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

    public static Data.LinkedData getLinkedData(UUID uuid) {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            statement.executeUpdate("USE " + Data.mysqlDatabaseName);
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);

            // データを1行ずつ処理
            while (resultSet.next()) {
                UUID mc_uuid = UUID.fromString(resultSet.getString(column_uuid));
                if (mc_uuid.equals(uuid)) {
                    Long dis_id = resultSet.getLong(column_discordID);
                    String creation_date = resultSet.getString(column_creationDate);

                    String[] splitDateTime = creation_date.split("_");
                    String[] date = splitDateTime[0].split("/");
                    String[] time = splitDateTime[1].split(":");

                    LocalDateTime localDateTime = LocalDateTime.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]), Integer.parseInt(time[0]), Integer.parseInt(time[1]));
                    return new Data.LinkedData(dis_id, localDateTime);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void addLinkData(UUID uuid, Data.LinkedData linkedData) {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String minecraft_uuid = uuid.toString();
            String discord_id = linkedData.getDiscordUserID().toString();
            String creation_date = linkedData.getLinkedLocalDateTime().getYear() + "/" + linkedData.getLinkedLocalDateTime().getMonthValue() + "/" + linkedData.getLinkedLocalDateTime().getDayOfMonth() + "_" +
                    linkedData.getLinkedLocalDateTime().getHour() + ":" + linkedData.getLinkedLocalDateTime().getMinute();

            statement.executeUpdate("USE " + Data.mysqlDatabaseName);
            statement.executeUpdate("INSERT INTO linked_data (" + column_uuid + ", " + column_discordID + ", " + column_creationDate + ") VALUES ('" + minecraft_uuid + "', '" + discord_id + "', '" + creation_date + "')");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeLinkData(UUID uuid) {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            statement.executeUpdate("USE " + Data.mysqlDatabaseName);
            statement.executeUpdate("DELETE FROM " + tableName + " WHERE `" + tableName + "`.`" + column_uuid + "` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
