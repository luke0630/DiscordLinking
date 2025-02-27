package org.luke.discordLinking;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MojangAPI {
    public static String getUsernameFromUUID(String uuid) {
        try {
            String urlString = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-","");
            URL url = new URL(urlString);

            HttpURLConnection connection;
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            // レスポンスコードを確認
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject responseJson = JsonParser.parseReader(reader).getAsJsonObject();

            // プロファイル名が存在するか確認
            if (responseJson.has("name")) {
                return responseJson.get("name").getAsString();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
