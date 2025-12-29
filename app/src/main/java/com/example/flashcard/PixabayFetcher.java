package com.example.flashcard;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;

public class PixabayFetcher {

    private static final String API_KEY = "53606559-5f8d8a317e7f8da942e7b81ae";
    private static final String BASE_URL = "https://pixabay.com/api/";

    private final OkHttpClient client;

    public PixabayFetcher() {
        client = new OkHttpClient();
    }

    public String searchImage(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = BASE_URL + "?key=" + API_KEY
                    + "&q=" + encodedQuery
                    + "&lang=en"
                    + "&image_type=photo"
                    + "&per_page=3"
                    + "&safesearch=true";

            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray hits = jsonObject.getJSONArray("hits");

                    if (hits.length() > 0) {
                        return hits.getJSONObject(0).getString("webformatURL");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("PixabayFetcher", "Lỗi: " + e.getMessage());
        }
        return null;
    }
}