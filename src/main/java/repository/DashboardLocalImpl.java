package repository;

import database.ConexionDB;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DashboardLocalImpl {

    public DashboardLocalImpl() {
        crearTablasSiNoExisten();
    }

    private void crearTablasSiNoExisten() {
        String sqlRetiros = "CREATE TABLE IF NOT EXISTS retiros_caja ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "vendedor VARCHAR(50), "
                + "monto DECIMAL(10,2), "
                + "motivo VARCHAR(255), "
                + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "sincronizado INTEGER DEFAULT 0)";
                
        String sqlEntradas = "CREATE TABLE IF NOT EXISTS entradas_inventario ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "id_producto VARCHAR(50), "
                + "cantidad INT, "
                + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "sincronizado INTEGER DEFAULT 0)";

        try (Connection conn = ConexionDB.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlRetiros);
            stmt.execute(sqlEntradas);
            
            // Migración automática
            try { stmt.execute("ALTER TABLE retiros_caja ADD COLUMN sincronizado INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE entradas_inventario ADD COLUMN sincronizado INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
        } catch (SQLException ignored) {}
    }

    // --- MÉTODOS PARA LOS RETIROS ---
    public boolean registrarRetiro(String vendedor, double monto, String motivo) {
        String sql = "INSERT INTO retiros_caja (id, vendedor, monto, motivo) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, UUID.randomUUID().toString());
            pst.setString(2, vendedor);
            pst.setDouble(3, monto);
            pst.setString(4, motivo);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public double calcularRetirosTurnoActual(String fechaApertura) {
        String sql = "SELECT SUM(monto) as total_retiros FROM retiros_caja WHERE fecha >= ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, fechaApertura);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getDouble("total_retiros");
            }
        } catch (SQLException e) {}
        return 0.0;
    }

    // --- NUEVO: MÉTODO PARA REGISTRAR ENTRADAS DE MERCANCÍA ---
    // Debes llamar a este método cada vez que le sumes stock a un producto desde tu catálogo
    public boolean registrarEntradaMercancia(String idProducto, int cantidadAgregada) {
        if (cantidadAgregada <= 0) return false; // Solo registramos entradas positivas
        
        String sql = "INSERT INTO entradas_inventario (id, id_producto, cantidad) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, UUID.randomUUID().toString());
            pst.setString(2, idProducto);
            pst.setInt(3, cantidadAgregada);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private String getFechaHoyParaBusqueda() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "%";
    }

    public double getVentasTotalesHoy() {
        String sql = "SELECT SUM(total) as total FROM ventas WHERE fecha LIKE ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, getFechaHoyParaBusqueda());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {}
        return 0.0;
    }

    public double getRetirosTotalesHoy() {
        String sql = "SELECT SUM(monto) as total FROM retiros_caja WHERE fecha LIKE ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, getFechaHoyParaBusqueda());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {}
        return 0.0;
    }

    public int getCantidadVentasHoy() {
        String sql = "SELECT COUNT(id) as total FROM ventas WHERE fecha LIKE ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, getFechaHoyParaBusqueda());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {}
        return 0;
    }

    public int getArticulosVendidosHoy() {
        String sql = "SELECT SUM(vd.cantidad) as total FROM venta_detalles vd JOIN ventas v ON vd.venta_id = v.id WHERE v.fecha LIKE ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, getFechaHoyParaBusqueda());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener artículos vendidos: " + e.getMessage());
        }
        return 0;
    }

    public int getEntradasInventarioHoy() {
        String sql = "SELECT SUM(cantidad) as total FROM entradas_inventario WHERE fecha LIKE ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, getFechaHoyParaBusqueda());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {}
        return 0;
    }

    public int getProductosBajoStock() {
        String sql = "SELECT COUNT(codigo_barras) as total FROM productos WHERE stock_actual <= stock_critico AND stock_critico > 0";
        
        try (Connection conn = ConexionDB.conectar(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
             
            if (rs.next()) return rs.getInt("total");
            
        } catch (SQLException e) {
            System.err.println("Error consultando bajo stock: " + e.getMessage());
        }
        return 0;
    }
}