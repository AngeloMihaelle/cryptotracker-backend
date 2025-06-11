package com.backend.services;

import com.google.gson.*;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            if (!response.isSuccessful()) {
                System.err.println("Error al obtener el top 10: " + response.code() + " - " + response.message());
                throw new IOException("Unexpected code " + response);
            }

            if (response.body() == null) {
                throw new IOException("Cuerpo de respuesta vacío");
            }

            String body = response.body().string();
            return gson.fromJson(body, new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>(){}.getType());
        }
    }

    public List<List<Double>> getPriceHistory(String coinId, int hours) throws IOException {
        System.out.println("[DEBUG] getPriceHistory mock mode: leyendo btc_fake_data.csv para " + coinId);

        List<List<Double>> prices = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/btc_fake_data.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line = br.readLine(); // salta encabezado
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                long tsSeconds = Long.parseLong(parts[0]);
                double price = Double.parseDouble(parts[1]);
                // convertir a ms y simular array [timestamp_ms, price]
                prices.add(List.of(tsSeconds * 1000.0, price));
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Al leer CSV dummy: " + e.getMessage());
            throw new IOException("Mock data fail", e);
        }

        System.out.println("[DEBUG] Precio histórico cargado. Puntos: " + prices.size());
        return prices;
    }
    public void updatePrices() {
        // En este ejemplo, no se almacena localmente, así que no hace nada
        System.out.println("Forzando actualización (no hay cache implementado)");
    }
}
