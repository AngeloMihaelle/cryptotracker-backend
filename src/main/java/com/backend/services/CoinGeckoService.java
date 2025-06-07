package com.backend.services;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

public class CoinGeckoService {
    private static final String BASE_URL = "https://api.coingecko.com/api/v3";
    private final OkHttpClient httpClient;
    private final Gson gson;

    public CoinGeckoService() {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    public List<Map<String, Object>> getTop10Coins() throws IOException {
        String url = BASE_URL + "/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=10&page=1";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String body = response.body().string();
            return gson.fromJson(body, new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>(){}.getType());
        }
    }

    public List<List<Double>> getPriceHistory(String coinId, int hours) throws IOException {
        // CoinGecko solo permite granularidad horaria hasta 90 días atrás
        int days = Math.max(1, (int) Math.ceil(hours / 24.0));

        String url = BASE_URL + "/coins/" + coinId.toLowerCase()
                + "/market_chart?vs_currency=usd&days=" + days + "&interval=hourly";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray pricesArray = json.getAsJsonArray("prices");

            List<List<Double>> prices = new ArrayList<>();
            for (JsonElement e : pricesArray) {
                JsonArray pair = e.getAsJsonArray();
                prices.add(Arrays.asList(pair.get(0).getAsDouble(), pair.get(1).getAsDouble()));
            }
            return prices;
        }
    }

    public void updatePrices() {
        // En este ejemplo, no se almacena localmente, así que no hace nada
        System.out.println("Forzando actualización (no cache implementado aún)");
    }
}
