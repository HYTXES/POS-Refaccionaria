package repository;

import database.ConexionDB;
import java.sql.*;
import java.util.UUID;

public class CajaLocalImpl {

    public CajaLocalImpl() {
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS control_caja ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "vendedor VARCHAR(50), "
                + "fondo_inicial DECIMAL(10,2), "
                + "ventas_turno DECIMAL(10,2) DEFAULT 0, "
                + "total_real DECIMAL(10,2) DEFAULT 0, "
                + "fecha_apertura DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "fecha_cierre DATETIME, "
                + "estado VARCHAR(20) DEFAULT 'ABIERTA', "
                + "sincronizado INTEGER DEFAULT 0)";

        try (Connection conn = ConexionDB.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            try { stmt.execute("ALTER TABLE control_caja ADD COLUMN sincronizado INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            
            // --- AUTO-SANACIÓN: Convierte los estados nulos o fantasmas en ABIERTA ---
            try { stmt.execute("UPDATE control_caja SET estado = 'ABIERTA' WHERE estado IS NULL"); } catch (SQLException ignored) {}
        } catch (SQLException ignored) {}
    }

    public boolean hayCajaAbierta() {
        // Buscar cualquier registro que diga ABIERTA
        String sql = "SELECT id FROM control_caja WHERE UPPER(estado) = 'ABIERTA' LIMIT 1";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public void abrirCaja(String vendedor, double fondo) {
        // INSERCIÓN EXPLÍCITA DEL ESTADO 'ABIERTA'
        String sql = "INSERT INTO control_caja (id, vendedor, fondo_inicial, estado, sincronizado) VALUES (?, ?, ?, 'ABIERTA', 0)";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, UUID.randomUUID().toString());
            pst.setString(2, vendedor);
            pst.setDouble(3, fondo);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double obtenerFondoInicial() {
        String sql = "SELECT fondo_inicial FROM control_caja WHERE UPPER(estado) = 'ABIERTA' LIMIT 1";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getDouble("fondo_inicial");
        } catch (SQLException e) {}
        return 0.0;
    }

    public double calcularVentasTurno() {
        String sql = "SELECT SUM(total) as total_ventas FROM ventas WHERE fecha >= (SELECT fecha_apertura FROM control_caja WHERE UPPER(estado) = 'ABIERTA' LIMIT 1)";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getDouble("total_ventas");
        } catch (SQLException e) {}
        return 0.0;
    }

    public boolean cerrarCaja(double totalReal) {
        double ventasTurno = calcularVentasTurno();
        String sql = "UPDATE control_caja SET estado = 'CERRADA', ventas_turno = ?, total_real = ?, fecha_cierre = CURRENT_TIMESTAMP, sincronizado = 0 WHERE UPPER(estado) = 'ABIERTA'";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setDouble(1, ventasTurno);
            pst.setDouble(2, totalReal);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public java.util.List<String[]> obtenerHistorialCortes() {
        java.util.List<String[]> lista = new java.util.ArrayList<>();
        String sql = "SELECT id, vendedor, fondo_inicial, ventas_turno, total_real, fecha_cierre, estado FROM control_caja ORDER BY fecha_cierre DESC";
        
        try (java.sql.Connection conn = database.ConexionDB.conectar(); 
             java.sql.Statement stmt = conn.createStatement(); 
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                double fondo = rs.getDouble("fondo_inicial");
                double ventas = rs.getDouble("ventas_turno");
                double real = rs.getDouble("total_real");
                double diff = real - (fondo + ventas);
                
                String fecha = rs.getString("fecha_cierre");
                if (fecha == null) fecha = "EN CURSO (Abierta)";

                lista.add(new String[]{
                    rs.getString("id"), 
                    fecha,
                    rs.getString("vendedor"),
                    "$" + String.format("%.2f", fondo),
                    "$" + String.format("%.2f", ventas),
                    "$" + String.format("%.2f", real),
                    "$" + String.format("%.2f", diff),
                    rs.getString("estado")
                });
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean eliminarCorte(String id) {
        String sql = "DELETE FROM control_caja WHERE id = ?";
        try (java.sql.Connection conn = database.ConexionDB.conectar(); 
             java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, id);
            return pst.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            return false;
        }
    }
    
    public String obtenerFechaAperturaActual() {
        String sql = "SELECT fecha_apertura FROM control_caja WHERE UPPER(estado) = 'ABIERTA' LIMIT 1";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getString("fecha_apertura");
        } catch (SQLException e) {}
        return null;
    }
}