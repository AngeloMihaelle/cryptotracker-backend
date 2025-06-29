package com.backend.handlers;

import com.backend.services.CoinGeckoService;
import com.sun.net.httpserver.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.List;


public class RegressionChartHandler implements HttpHandler {
    private final CoinGeckoService svc;

    public RegressionChartHandler(CoinGeckoService svc) {
        this.svc = svc;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        System.out.println("[INFO] /api/chart/regression invoked: " + ex.getRequestURI());

        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }

        URI uri = ex.getRequestURI();
        String[] parts = uri.getPath().split("/");

        if (parts.length < 5) {
            ex.sendResponseHeaders(400, -1);
            return;
        }

        String symbol = parts[4];
        Map<String, String> params = queryToMap(uri.getRawQuery());
        String hoursParam = params.get("hours");

        if (hoursParam == null) {
            ex.sendResponseHeaders(400, -1);
            return;
        }

        int hours;
        try {
            hours = Integer.parseInt(hoursParam);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid hours parameter: " + hoursParam);
            ex.sendResponseHeaders(400, -1);
            return;
        }

        List<Map<String, Object>> history = svc.getPriceHistory(symbol, hours);

        if (history.isEmpty()) {
            ex.sendResponseHeaders(404, -1);
            return;
        }

        TimeSeries series = new TimeSeries(symbol.toUpperCase());
        SimpleRegression regression = new SimpleRegression(true);

        long minX = Long.MAX_VALUE;
        long maxX = Long.MIN_VALUE;

        for (Map<String, Object> entry : history) {
            long tsMs = ((Number) entry.get("timestamp")).longValue();
            double price = ((Number) entry.get("price")).doubleValue();

            regression.addData(tsMs, price);
            series.addOrUpdate(new Minute(new Date(tsMs)), price);

            if (tsMs < minX) minX = tsMs;
            if (tsMs > maxX) maxX = tsMs;
        }

        if (regression.getN() == 0) {
            ex.sendResponseHeaders(400, -1);
            return;
        }

        double intercept = regression.getIntercept();
        double slope = regression.getSlope();
        String eq = String.format("y = %.4f + %.4f * t", intercept, slope);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        TimeSeries regSeries = new TimeSeries("Regresión");
        double yMin = intercept + slope * minX;
        double yMax = intercept + slope * maxX;
        regSeries.addOrUpdate(new Minute(new Date(minX)), yMin);
        regSeries.addOrUpdate(new Minute(new Date(maxX)), yMax);
        dataset.addSeries(regSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                symbol.toUpperCase() + " Regresión: " + eq,
                "Fecha y Hora", "USD", dataset,
                true, false, false
        );

        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer(true, false); // Línea y puntos
        renderer1.setSeriesPaint(0, Color.CYAN);
        renderer1.setSeriesStroke(0, new BasicStroke(2f));
        renderer1.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));

        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(true, false); // Solo línea
        renderer2.setSeriesPaint(0, Color.ORANGE);
        renderer2.setSeriesStroke(0, new BasicStroke(3f));

        plot.setRenderer(0, renderer1);
        plot.setRenderer(1, renderer2);

        ex.getResponseHeaders().add("Content-Type", "image/png");
        ex.sendResponseHeaders(200, 0);
        try (OutputStream os = ex.getResponseBody()) {
            ChartUtils.writeChartAsPNG(os, chart, 1000, 600);
            System.out.println("[INFO] Regression chart sent.");
        }
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> map = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
