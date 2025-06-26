package com.backend.handlers;

import com.backend.services.DatabaseService;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ExportCsvHandler implements HttpHandler {
    private final DatabaseService databaseService;

    public ExportCsvHandler(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        URI uri = exchange.getRequestURI();
        String[] parts = uri.getPath().split("/");

        if (parts.length < 4) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String symbol = parts[3].toUpperCase();

        List<Map<String, Object>> history;
        try {
            history = databaseService.getPriceHistory(symbol, 100000); // Arbitrariamente grande para traer todo
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, -1);
            return;
        }

        if (history.isEmpty()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // Crear CSV en memoria
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));

        writer.println("timestamp,price");

        for (Map<String, Object> entry : history) {
            long timestamp = ((Number) entry.get("timestamp")).longValue();
            Object price = entry.get("price");
            writer.printf("%d,%s\n", timestamp, price.toString());
        }

        writer.flush();

        // Preparar respuesta
        byte[] csvBytes = outputStream.toByteArray();
        exchange.getResponseHeaders().add("Content-Type", "text/csv");
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + symbol + "_history.csv\"");
        exchange.sendResponseHeaders(200, csvBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(csvBytes);
        }
    }
}
