package com.backend.handlers;

import com.backend.services.CoinGeckoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class PriceHistoryHandler implements HttpHandler {
    private final CoinGeckoService coinGeckoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PriceHistoryHandler(CoinGeckoService coinGeckoService) {
        this.coinGeckoService = coinGeckoService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            URI requestURI = exchange.getRequestURI();
            String path = requestURI.getPath();
            String[] segments = path.split("/");

            if (segments.length < 5) {
                exchange.sendResponseHeaders(400, -1); // Solicitud incorrecta
                return;
            }

            String symbol = segments[4]; // /api/prices/history/{symbol}
            Map<String, String> queryParams = QueryParser.parse(requestURI.getQuery());

            int hours;
            try {
                hours = Integer.parseInt(queryParams.getOrDefault("hours", "24"));
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1); // Parámetro inválido
                return;
            }

            try {
                // Ahora obtenemos lista de Map<String, Object> en vez de List<List<Double>>
                List<Map<String, Object>> priceHistory = coinGeckoService.getPriceHistory(symbol, hours);
                String response = objectMapper.writeValueAsString(priceHistory);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (IOException e) {
                exchange.sendResponseHeaders(500, -1); // Error del servidor
                e.printStackTrace();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Método no permitido
        }
    }
}
