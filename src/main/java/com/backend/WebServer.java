package com.backend;

import com.backend.handlers.*;
import com.backend.services.CoinGeckoService;
import com.backend.services.DatabaseService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    private static final int PORT = 80;

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            CoinGeckoService coinGeckoService = new CoinGeckoService();

            server.createContext("/api/health", new HealthHandler());
            server.createContext("/api/update-prices", new UpdatePricesHandler(coinGeckoService));
            server.createContext("/api/prices/latest", new LatestPricesHandler(coinGeckoService));
            server.createContext("/api/prices/history", new PriceHistoryHandler(coinGeckoService));
            server.createContext("/api/chart/single", new SingleChartHandler(coinGeckoService));

            server.createContext("/", new StaticFileHandler("/ui_assets"));
            server.createContext("/api/chart/overlay", new OverlayChartHandler(coinGeckoService));
            server.createContext("/api/chart/regression", new RegressionChartHandler(coinGeckoService));

            // PriceHistoryHandler debe manejar paths /api/prices/history/{symbol}
            server.createContext("/api/export/", new ExportCsvHandler(new DatabaseService()));

            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
            

            System.out.println("Servidor escuchando en el puerto " + PORT);
        } catch (IOException e) {
            System.err.println("Error iniciando el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
