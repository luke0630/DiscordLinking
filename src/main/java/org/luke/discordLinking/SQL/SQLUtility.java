package org.luke.discordLinking.SQL;

import com.velocitypowered.api.proxy.Player;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.UUID;

import static org.luke.discordLinking.SQL.SQLManager.*;

@UtilityClass
public class SQLUtility {
    public void putData(Long discordID, UUID mcUUID) {
        try {
            JSONArray jsonArray = getLinkedDataByDiscordID(discordID);
            if(jsonArray != null) {
                boolean alreadyLinked = false;
                for(int i=0;i < jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String uuid = jsonObject.getString("uuid");

                    if(Objects.equals(uuid, mcUUID.toString())) {
                        alreadyLinked = true;
                        break;
                    }
                }

                if(alreadyLinked) {
                    System.out.println("すでに紐づけ済みです uuid:" + mcUUID);
                } else {
                    jsonArray.put(getJSONData(mcUUID));

                    String sql = "UPDATE "+ tableName +" SET "+ column_linked_data +" = ? WHERE "+ column_discordID +" = ?";
                    PreparedStatement update = connection.prepareStatement(sql);

                    update.setString(1, jsonArray.toString());
                    update.setString(2, discordID.toString());

                    update.executeUpdate();
                }
            } else {
                System.out.println("存在しないため作成しました");
                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO " + SQLManager.tableName + " (" + SQLManager.column_discordID + ", " + column_linked_data + ") VALUES (?, ?)"
                );

                jsonArray = new JSONArray();
                jsonArray.put(getJSONData(mcUUID));

                insert.setLong(1, discordID);
                insert.setString(2, jsonArray.toString());

                insert.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public JSONArray getLinkedDataByDiscordID(Long discordID) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM "+ SQLManager.tableName +" WHERE "+ SQLManager.column_discordID +" = ?");
            stmt.setLong(1, discordID);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return new JSONArray(rs.getString(column_linked_data));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public JSONObject getJSONData(UUID mcUUID) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedString = formatter.format(timestamp);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", mcUUID.toString());
        jsonObject.put("timestamp", formattedString);

        return jsonObject;
    }

    public String getDiscordIdByUUID(UUID mcUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM "+ SQLManager.tableName);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                JSONArray jsonArray = new JSONArray(rs.getString(column_linked_data));
                for(int i=0;i < jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String uuid = jsonObject.getString("uuid");

                    if(uuid.equals(mcUUID.toString())) {
                        return rs.getString(column_discordID);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void unlinkMinecraftAccount(Long discordID, Player player) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM "+ SQLManager.tableName +" WHERE " + column_discordID + " = ?");
            stmt.setLong(1, discordID);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                JSONArray jsonArray = new JSONArray(rs.getString(column_linked_data));
                for(int i=0;i < jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String uuid = jsonObject.getString("uuid");
                    if(Objects.equals(uuid, player.getUniqueId().toString())) {
                        jsonArray.remove(i);

                        if(jsonArray.isEmpty()) {
                            PreparedStatement update = connection.prepareStatement("DELETE FROM "+ tableName +" WHERE "+ column_discordID +" = ?");
                            update.setLong(1, discordID);
                            update.executeUpdate();
                        } else {
                            String sql = "UPDATE "+ tableName +" SET "+ column_linked_data +" = ? WHERE "+ column_discordID +" = ?";
                            PreparedStatement update = connection.prepareStatement(sql);
                            update.setLong(1, discordID);

                            update.setString(1, jsonArray.toString());
                            update.setString(2, discordID.toString());

                            update.executeUpdate();
                        }

                        player.disconnect(
                                Component.text(
                                        "リンク解除されたため切断されました。"
                                )
                        );
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
