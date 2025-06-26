package com.backend.services;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class CoinGeckoService {
    private static final String BASE_URL = "https://api.coingecko.com/api/v3";
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final DatabaseService databaseService;

    public CoinGeckoService() {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
        this.databaseService = new DatabaseService();
    }

    // MÉTODO MODIFICADO: Ahora lee de la base de datos local con filtro de activas
    public List<Map<String, Object>> getTop10Coins() throws IOException {
        try {
            return databaseService.getLatestPrices();
        } catch (SQLException e) {
            throw new IOException("Error accediendo a la base de datos: " + e.getMessage(), e);
        }
    }

    // MÉTODO MODIFICADO: Ahora devuelve el formato correcto con filtro de activas
    public List<Map<String, Object>> getPriceHistory(String symbol, int hours) throws IOException {
        try {
            return databaseService.getPriceHistory(symbol, hours);
        } catch (SQLException e) {
            throw new IOException("Error accediendo al historial en BD: " + e.getMessage(), e);
        }
    }

    // ⚡ MÉTODO PRINCIPAL MODIFICADO: Mapeo actualizado para las 10 nuevas criptomonedas
    private String mapSymbolToCoinGeckoId(String symbol) {
        Map<String, String> mapping = new HashMap<>();
        
        // ✅ NUEVAS 10 CRIPTOMONEDAS (Actualizado Junio 2025)
        mapping.put("BTC", "bitcoin");           // Bitcoin ✅
        mapping.put("ETH", "ethereum");          // Ethereum ✅
        mapping.put("XRP", "ripple");            // XRP ✅
        mapping.put("SOL", "solana");            // Solana ✅
        mapping.put("TRX", "tron");              // TRON ⚡ NUEVA
        mapping.put("DOGE", "dogecoin");         // Dogecoin ✅
        mapping.put("ADA", "cardano");           // Cardano ✅
        mapping.put("HYPE", "hyperliquid");      // Hyperliquid ⚡ NUEVA
        mapping.put("BCH", "bitcoin-cash");      // Bitcoin Cash ⚡ NUEVA
        mapping.put("LINK", "chainlink");        // Chainlink ⚡ NUEVA
        
        // 🗑️ REMOVIDAS: Ya no están en la lista activa
        // mapping.put("USDT", "tether");
        // mapping.put("BNB", "binancecoin");
        // mapping.put("USDC", "usd-coin");
        // mapping.put("AVAX", "avalanche-2");
        
        return mapping.getOrDefault(symbol.toUpperCase(), symbol.toLowerCase());
    }

    // ⚡ MÉTODO OPTIMIZADO: Para el servicio de recolección de datos
    public void fetchAndStoreLatestPrices() throws IOException {
        System.out.println(" Obteniendo precios actuales de CoinGecko...");
        
        try {
            // Obtener lista de criptomonedas activas configuradas en BD
            List<Map<String, Object>> cryptos = databaseService.getAllCryptocurrencies();
            
            if (cryptos.isEmpty()) {
                System.err.println(" No hay criptomonedas activas en la base de datos");
                return;
            }
            
            // Construir lista de IDs para la API de CoinGecko
            List<String> coinIds = new ArrayList<>();
            Map<String, String> symbolToId = new HashMap<>();
            
            for (Map<String, Object> crypto : cryptos) {
                String symbol = (String) crypto.get("symbol");
                String coinId = mapSymbolToCoinGeckoId(symbol);
                coinIds.add(coinId);
                symbolToId.put(coinId, symbol);
            }
            
            System.out.println(" Recolectando precios para: " + String.join(", ", 
                symbolToId.values().toArray(new String[0])));
            
            // Llamar a la API de CoinGecko
            String url = BASE_URL + "/simple/price?ids=" + String.join(",", coinIds) + "&vs_currencies=usd";
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error en API CoinGecko: " + response.code());
                }

                String responseBody = response.body().string();
                JsonObject prices = JsonParser.parseString(responseBody).getAsJsonObject();
                
                int successCount = 0;
                int totalCount = coinIds.size();
                
                // Guardar cada precio en la base de datos
                for (String coinId : coinIds) {
                    if (prices.has(coinId)) {
                        JsonObject coinPrice = prices.getAsJsonObject(coinId);
                        if (coinPrice.has("usd")) {
                            double price = coinPrice.get("usd").getAsDouble();
                            String symbol = symbolToId.get(coinId);
                            
                            databaseService.insertPrice(symbol, price);
                            System.out.println("💾 Guardado: " + symbol + " = $" + String.format("%.4f", price));
                            successCount++;
                        } else {
                            System.err.println(" Precio USD no disponible para " + coinId);
                        }
                    } else {
                        System.err.println(coinId + " no encontrado en respuesta de CoinGecko");
                    }
                }
                
                if (successCount == totalCount) {
                    System.out.println("✅ Actualización completada exitosamente: " + successCount + "/" + totalCount + " criptos");
                } else {
                    System.out.println("⚠️ Actualización parcial: " + successCount + "/" + totalCount + " criptos guardadas");
                }
                
            }
        } catch (SQLException e) {
            throw new IOException("Error guardando en base de datos: " + e.getMessage(), e);
        }
    }

    // MÉTODO MODIFICADO: Ahora solo fuerza una actualización optimizada
    public void updatePrices() {
        try {
            fetchAndStoreLatestPrices();
        } catch (IOException e) {
            System.err.println(" Error en actualización forzada: " + e.getMessage());
        }
    }

    // ⚡ NUEVO MÉTODO: Verificar conectividad específica para nuevas criptos
    public boolean verifyNewCryptosAvailability() {
        String[] newCryptos = {"TRX", "HYPE", "BCH", "LINK"};
        boolean allAvailable = true;
        
        System.out.println(" Verificando disponibilidad de nuevas criptomonedas...");
        
        for (String symbol : newCryptos) {
            String coinId = mapSymbolToCoinGeckoId(symbol);
            String url = BASE_URL + "/simple/price?ids=" + coinId + "&vs_currencies=usd";
            
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        System.out.println("✅ " + symbol + " (" + coinId + ") disponible en CoinGecko");
                    } else {
                        System.err.println("❌ Error obteniendo " + symbol + " (" + coinId + "): " + response.code());
                        allAvailable = false;
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error verificando " + symbol + ": " + e.getMessage());
                allAvailable = false;
            }
        }
        
        return allAvailable;
    }

    // Test de conectividad para health check
    public boolean testApiConnectivity() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/ping")
                    .get()
                    .build();
                    
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            System.err.println("❌ Error de conectividad con CoinGecko: " + e.getMessage());
            return false;
        }
    }
}
