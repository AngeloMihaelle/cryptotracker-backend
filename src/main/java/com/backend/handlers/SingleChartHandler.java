package com.backend.handlers;

import com.backend.services.CoinGeckoService;
import com.sun.net.httpserver.*;
import org.jfree.chart.*;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SingleChartHandler implements HttpHandler {
    private final CoinGeckoService svc;

    public SingleChartHandler(CoinGeckoService svc) {
        this.svc = svc;
    }

    @Override
public void handle(HttpExchange ex) throws IOException {
        System.out.println("[INFO] Solicitud recibida en /api/chart/single");

        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            System.out.println("[WARN] Método no permitido: " + ex.getRequestMethod());
            ex.sendResponseHeaders(405, -1);
            return;
        }

        URI uri = ex.getRequestURI();
        System.out.println("[DEBUG] URI solicitada: " + uri.toString());

        String symbol = extractSymbol(uri.getPath(), "/api/chart/single/");
        if (symbol == null || symbol.isEmpty()) {
            System.out.println("[ERROR] Símbolo de criptomoneda no proporcionado en la ruta.");
            ex.sendResponseHeaders(400, -1);
            return;
        }
        System.out.println("[INFO] Símbolo solicitado: " + symbol);

        int hours = 24;
        String hParam = getParam(uri, "hours");
        if (hParam != null) {
            try {
                hours = Integer.parseInt(hParam);
                System.out.println("[DEBUG] Horas solicitadas: " + hours);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Parámetro 'hours' inválido: " + hParam);
                ex.sendResponseHeaders(400, -1);
                return;
            }
        }

        List<Map<String, Object>> rawHistory;
        try {
            rawHistory = svc.getPriceHistory(symbol, hours);
            System.out.println("[INFO] Historia de precios obtenida. Cantidad de puntos: " + rawHistory.size());
        } catch (Exception e) {
            System.out.println("[ERROR] Error al obtener historial de precios: " + e.getMessage());
            ex.sendResponseHeaders(500, -1);
            return;
        }

        JFreeChart chart;
        try {
            // Convertir a List<List<Double>> si ChartBuilder aún no soporta Map
            List<List<Double>> history = new ArrayList<>();
            for (Map<String, Object> point : rawHistory) {
                long timestamp = ((Number) point.get("timestamp")).longValue();
                double price = ((Number) point.get("price")).doubleValue();
                history.add(Arrays.asList((double) timestamp, price));
            }

            chart = ChartBuilder.buildStyledLineChart(history, "Precio de " + symbol, "Hora", "USD");
            System.out.println("[INFO] Gráfico generado correctamente.");
        } catch (Exception e) {
            System.out.println("[ERROR] Error al generar el gráfico: " + e.getMessage());
            ex.sendResponseHeaders(500, -1);
            return;
        }

        ex.getResponseHeaders().add("Content-Type", "image/png");
        ex.sendResponseHeaders(200, 0);
        try (OutputStream os = ex.getResponseBody()) {
            ChartUtils.writeChartAsPNG(os, chart, 800, 600);
            System.out.println("[INFO] Imagen enviada al cliente correctamente.");
        } catch (Exception e) {
            System.out.println("[ERROR] Error al escribir imagen en respuesta: " + e.getMessage());
        }
    }

    private String extractSymbol(String path, String prefix) {
        return path.startsWith(prefix) ? path.substring(prefix.length()) : "";
    }

    private String getParam(URI uri, String name) {
        if (uri.getQuery() == null) return null;
        for (String kv : uri.getQuery().split("&")) {
            String[] p = kv.split("=");
            if (p.length == 2 && p[0].equals(name)) return p[1];
        }
        return null;
    }
}
