package repository;

import database.ConexionLocal;
import entities.Venta;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.table.DefaultTableModel;

public class VentaLocalImpl {

    public VentaLocalImpl() {
        crearTablasSiNoExisten();
    }

    private void crearTablasSiNoExisten() {
        String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "total DECIMAL(10,2), "
                + "vendedor VARCHAR(50), "
                + "sincronizado INTEGER DEFAULT 0)";

        String sqlDetalles = "CREATE TABLE IF NOT EXISTS venta_detalles ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "venta_id VARCHAR(36), "
                + "codigo_producto VARCHAR(50), "
                + "cantidad INTEGER, "
                + "subtotal DECIMAL(10,2), "
                + "FOREIGN KEY(venta_id) REFERENCES ventas(id))";

        try (Connection conn = ConexionLocal.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlVentas);
            stmt.execute(sqlDetalles);
            
            try { stmt.execute("ALTER TABLE ventas ADD COLUMN vendedor VARCHAR(50)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE ventas ADD COLUMN sincronizado INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            
        } catch (SQLException e) {
        }
    }

    public String procesarVenta(DefaultTableModel carrito, double total, String vendedor) {
        String idVenta = UUID.randomUUID().toString();
        String insertVenta = "INSERT INTO ventas(id, total, vendedor, sincronizado) VALUES(?, ?, ?, 0)";
        String insertDetalle = "INSERT INTO venta_detalles(id, venta_id, codigo_producto, cantidad, subtotal) VALUES(?, ?, ?, ?, ?)";
        String updateStock = "UPDATE productos SET stock_actual = stock_actual - ?, sincronizado = 0 WHERE codigo_barras = ?";
        
        Connection conn = null;
        try {
            conn = ConexionLocal.conectar();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstVenta = conn.prepareStatement(insertVenta)) {
                pstVenta.setString(1, idVenta);
                pstVenta.setDouble(2, total);
                pstVenta.setString(3, vendedor);
                pstVenta.executeUpdate();
            }

            try (PreparedStatement pstDetalle = conn.prepareStatement(insertDetalle);
                 PreparedStatement pstStock = conn.prepareStatement(updateStock)) {

                for (int i = 0; i < carrito.getRowCount(); i++) {
                    int cantidad = (int) carrito.getValueAt(i, 0);
                    String codigo = carrito.getValueAt(i, 1).toString();
                    double subtotal = (double) carrito.getValueAt(i, 4);

                    pstDetalle.setString(1, UUID.randomUUID().toString());
                    pstDetalle.setString(2, idVenta);
                    pstDetalle.setString(3, codigo);
                    pstDetalle.setInt(4, cantidad);
                    pstDetalle.setDouble(5, subtotal);
                    pstDetalle.executeUpdate();

                    pstStock.setInt(1, cantidad);
                    pstStock.setString(2, codigo);
                    pstStock.executeUpdate();
                }
            }

            conn.commit(); 
            return idVenta;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { } 
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { }
            }
        }
    }

    public List<Venta> obtenerHistorial() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas ORDER BY fecha DESC";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        try (Connection conn = ConexionLocal.conectar(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Venta v = new Venta(rs.getString("id"));
                String fechaStr = rs.getString("fecha");
                if (fechaStr != null) {
                    try {
                        v.setFechaVenta(sdf.parse(fechaStr));
                    } catch (Exception ex) {
                        v.setFechaVenta(new java.util.Date());
                    }
                }
                v.setTotal(rs.getDouble("total"));
                v.setVendedor(rs.getString("vendedor"));
                
                try {
                    v.setSincronizado(rs.getInt("sincronizado") == 1);
                } catch (Exception ignored) {} 
                
                lista.add(v);
            }
        } catch (SQLException e) {
        }
        return lista;
    }

    public boolean eliminarVenta(String idVenta) {
        String sqlDetalles = "DELETE FROM venta_detalles WHERE venta_id = ?";
        String sqlVenta = "DELETE FROM ventas WHERE id = ?";
        Connection conn = null;
        try {
            conn = ConexionLocal.conectar();
            conn.setAutoCommit(false);
            try (PreparedStatement pstDetalles = conn.prepareStatement(sqlDetalles)) {
                pstDetalles.setString(1, idVenta);
                pstDetalles.executeUpdate();
            }
            try (PreparedStatement pstVenta = conn.prepareStatement(sqlVenta)) {
                pstVenta.setString(1, idVenta);
                pstVenta.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { }
            }
        }
    }

    public List<String[]> obtenerDetallesParaReporte() {
        List<String[]> detalles = new ArrayList<>();
        
        String sql = "SELECT v.id, v.fecha, vd.codigo_producto, p.nombre, vd.cantidad, vd.subtotal, v.vendedor " +
                     "FROM venta_detalles vd " +
                     "JOIN ventas v ON vd.venta_id = v.id " +
                     "JOIN productos p ON vd.codigo_producto = p.codigo_barras " +
                     "ORDER BY v.fecha DESC";

        try (Connection conn = ConexionLocal.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                detalles.add(new String[]{
                    rs.getString("id") != null ? rs.getString("id").substring(0, 8).toUpperCase() : "N/A",
                    rs.getString("fecha"),
                    rs.getString("vendedor"),
                    rs.getString("codigo_producto"),
                    rs.getString("nombre"),
                    String.valueOf(rs.getInt("cantidad")),
                    "$" + String.format("%.2f", rs.getDouble("subtotal"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detalles;
    }
}