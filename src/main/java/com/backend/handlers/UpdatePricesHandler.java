package com.backend.handlers;

import com.backend.services.CoinGeckoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class UpdatePricesHandler implements HttpHandler {
    private final CoinGeckoService coinGeckoService;

    public UpdatePricesHandler(CoinGeckoService coinGeckoService) {
        this.coinGeckoService = coinGeckoService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            coinGeckoService.updatePrices();
            String response = "Actualización de precios forzada\n";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Método no permitido
        }
    }
}
