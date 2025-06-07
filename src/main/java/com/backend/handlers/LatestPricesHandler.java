package com.backend.handlers;

import com.backend.services.CoinGeckoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class LatestPricesHandler implements HttpHandler {
    private final CoinGeckoService coinGeckoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LatestPricesHandler(CoinGeckoService coinGeckoService) {
        this.coinGeckoService = coinGeckoService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> topCoins = coinGeckoService.getTop10Coins();
            String response = objectMapper.writeValueAsString(topCoins);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Método no permitido
        }
    }
}
