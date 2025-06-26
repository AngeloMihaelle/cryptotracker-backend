package com.backend.services;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class DatabaseService {
    // ⚡ MODIFICADO: Nueva base de datos
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", 
        "jdbc:postgresql://34.41.127.226:5432/cryptodb_v2");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "cryptouser");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "crypto123");
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL driver not found", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ⚡ MODIFICADO: Solo obtener criptomonedas activas
    public List<Map<String, Object>> getAllCryptocurrencies() throws SQLException {
        List<Map<String, Object>> cryptos = new ArrayList<>();
        String sql = "SELECT id, symbol, name, logo_url FROM cryptocurrencies WHERE active = TRUE ORDER BY id";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> crypto = new HashMap<>();
                crypto.put("id", rs.getInt("id"));
                crypto.put("symbol", rs.getString("symbol"));
                crypto.put("name", rs.getString("name"));
                crypto.put("logo_url", rs.getString("logo_url"));
                cryptos.add(crypto);
            }
        }
        return cryptos;
    }

    // ⚡ MODIFICADO: Solo últimos precios de criptomonedas activas
    public List<Map<String, Object>> getLatestPrices() throws SQLException {
        List<Map<String, Object>> prices = new ArrayList<>();
        String sql = "SELECT c.symbol, c.name, c.logo_url, ph.price_usd, ph.timestamp " +
                    "FROM cryptocurrencies c " +
                    "LEFT JOIN LATERAL ( " +
                    "    SELECT price_usd, timestamp " +
                    "    FROM price_history " +
                    "    WHERE crypto_id = c.id " +
                    "    ORDER BY timestamp DESC " +
                    "    LIMIT 1 " +
                    ") ph ON true " +
                    "WHERE c.active = TRUE " +
                    "ORDER BY c.id";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> price = new HashMap<>();
                price.put("symbol", rs.getString("symbol"));
                price.put("name", rs.getString("name"));
                price.put("logo_url", rs.getString("logo_url"));
                price.put("current_price", rs.getBigDecimal("price_usd"));
                price.put("last_updated", rs.getTimestamp("timestamp"));
                prices.add(price);
            }
        }
        return prices;
    }

    // ⚡ MODIFICADO: Historial solo de criptomonedas activas
    public List<Map<String, Object>> getPriceHistory(String symbol, int hours) throws SQLException {
        List<Map<String, Object>> history = new ArrayList<>();
        String sql = "SELECT ph.price_usd, ph.timestamp " +
                    "FROM price_history ph " +
                    "JOIN cryptocurrencies c ON c.id = ph.crypto_id " +
                    "WHERE c.symbol = ? AND c.active = TRUE " +
                    "AND ph.timestamp >= ? " +
                    "ORDER BY ph.timestamp ASC";
        
        Timestamp since = Timestamp.from(Instant.now().minusSeconds(hours * 3600L));
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, symbol.toUpperCase());
            stmt.setTimestamp(2, since);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("timestamp", rs.getTimestamp("timestamp").getTime());
                    point.put("price", rs.getBigDecimal("price_usd"));
                    history.add(point);
                }
            }
        }
        return history;
    }

    // ⚡ MODIFICADO: Solo insertar precios en criptomonedas activas
    public void insertPrice(String symbol, double price) throws SQLException {
        String sql = "INSERT INTO price_history (crypto_id, price_usd, timestamp) " +
                    "SELECT c.id, ?, CURRENT_TIMESTAMP " +
                    "FROM cryptocurrencies c " +
                    "WHERE c.symbol = ? AND c.active = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, new java.math.BigDecimal(price));
            stmt.setString(2, symbol.toUpperCase());
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                System.out.println(" Crypto " + symbol + " no está activa o no existe");
            }
        }
    }

    // Limpiar datos antiguos (mantener solo últimas 24 horas)
    public void cleanOldData() throws SQLException {
        String sql = "DELETE FROM price_history WHERE timestamp < ?";
        Timestamp cutoff = Timestamp.from(Instant.now().minusSeconds(24 * 3600L));
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, cutoff);
            int deleted = stmt.executeUpdate();
            System.out.println("Limpieza de BD: " + deleted + " registros antiguos eliminados");
        }
    }

    // Test de conexión para health check
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            System.err.println("Error de conexión a BD: " + e.getMessage());
            return false;
        }
    }
}
