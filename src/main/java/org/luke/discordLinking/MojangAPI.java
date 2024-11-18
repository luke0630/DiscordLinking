package org.luke.discordLinking;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MojangAPI {
    private static String getUsernameFromUUID(String uuid) throws Exception {
        String urlString = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-","");
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // レスポンスコードを確認
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Error: " + responseCode);
        }

        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        JsonObject responseJson = JsonParser.parseReader(reader).getAsJsonObject();

        // プロファイル名が存在するか確認
        if (responseJson.has("name")) {
            return responseJson.get("name").getAsString();
        } else {
            throw new Exception("Profile name not found or invalid UUID.");
        }
    }

    public static String getUserName(String uuid) {
        try {
            return getUsernameFromUUID(uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
