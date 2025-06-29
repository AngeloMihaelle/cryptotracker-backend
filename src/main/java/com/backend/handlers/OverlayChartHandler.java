package com.backend.handlers;

import com.backend.services.CoinGeckoService;
import com.sun.net.httpserver.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.ChartUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;

public class OverlayChartHandler implements HttpHandler {
    private final CoinGeckoService svc;

    public OverlayChartHandler(CoinGeckoService svc) {
        this.svc = svc;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        System.out.println("[INFO] /api/chart/overlay invoked: " + ex.getRequestURI());
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, String> params = queryToMap(ex.getRequestURI().getQuery());
        String symbolsParam = params.get("symbols");
        String hoursParam = params.get("hours");

        if (symbolsParam == null || hoursParam == null) {
            System.out.println("[ERROR] Missing required params: symbols, hours");
            ex.sendResponseHeaders(400, -1);
            return;
        }

        List<String> symbols = Arrays.asList(symbolsParam.split(","));
        int hours;
        try {
            hours = Integer.parseInt(hoursParam);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid hours value: " + e.getMessage());
            ex.sendResponseHeaders(400, -1);
            return;
        }

        long now = System.currentTimeMillis();
        long startTimeMs = now - hours * 3600L * 1000;

        TimeSeriesCollection dataset = new TimeSeriesCollection();

        for (String sym : symbols) {
            List<Map<String, Object>> history = svc.getPriceHistory(sym, hours);
            TimeSeries ts = new TimeSeries(sym.toUpperCase());

            for (Map<String, Object> p : history) {
                long tsMs = ((Number) p.get("timestamp")).longValue();
                double price = ((Number) p.get("price")).doubleValue();

                if (tsMs >= startTimeMs) {
                    ts.addOrUpdate(new Minute(new Date(tsMs)), price);
                }
            }

            dataset.addSeries(ts);
            System.out.println("[INFO] Added series '" + sym + "' with " + ts.getItemCount() + " points");
        }


        JFreeChart chart = ChartBuilder.buildStyledOverlayChart(
            dataset, "Overlay Chart", "Hora", "Datos Normalizados", symbols.size()
        );

        ex.getResponseHeaders().add("Content-Type", "image/png");
        ex.sendResponseHeaders(200, 0);
        try (OutputStream os = ex.getResponseBody()) {
            ChartUtils.writeChartAsPNG(os, chart, 1000, 600);
            System.out.println("[INFO] Overlay image sent.");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to write PNG: " + e.getMessage());
        }
    }

    // Utilidad para parsing de query params
    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) result.put(pair[0], pair[1]);
        }
        return result;
    }
}
