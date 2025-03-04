package org.luke.discordLinking.SQL;

import org.luke.discordLinking.Data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static org.luke.discordLinking.DiscordLinking.getInstance;

public class SQLManager {

    private static Connection connection = null;
    static final String tableName = "linked_data";
    static final String column_discordID = "discord_id";
    static final String column_linked_data = "link_data";

    public static void ConnectionToDatabase() {
        String url = "jdbc:mysql://" + Data.mysqlURL;
        try {
            // MySQLドライバのロード
            Class.forName("com.mysql.cj.jdbc.Driver");
            // データベースへの接続
            getInstance().getLogger().info("データベースに接続しました。");
            getInstance().getLogger().info("データベース名: {}", Data.mysqlDatabaseName);
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
            getInstance().getLogger().info("テーブル{}が作成されました。", tableName);

            for (String execute : executes) {
                statement.executeUpdate(execute);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
