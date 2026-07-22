package repository;

import database.ConexionLocal;
import entities.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorLocalImpl {

    public ProveedorLocalImpl() {
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS proveedores ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "nombre VARCHAR(100) NOT NULL, "
                + "contacto VARCHAR(100), "
                + "telefono VARCHAR(20), "
                + "correo VARCHAR(100), "
                + "sincronizado INTEGER DEFAULT 0)";

        try (Connection conn = ConexionLocal.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            // Migración automática
            try { stmt.execute("ALTER TABLE proveedores ADD COLUMN sincronizado INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
        } catch (SQLException ignored) {}
    }

    public boolean guardar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedores(id, nombre, contacto, telefono, correo) VALUES(?,?,?,?,?)";
        try (Connection conn = ConexionLocal.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, proveedor.getId());
            pstmt.setString(2, proveedor.getNombre());
            pstmt.setString(3, proveedor.getContacto());
            pstmt.setString(4, proveedor.getTelefono());
            pstmt.setString(5, proveedor.getCorreo());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Proveedor> obtenerTodos() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedores";
        try (Connection conn = ConexionLocal.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Proveedor p = new Proveedor(rs.getString("id"));
                p.setNombre(rs.getString("nombre"));
                p.setContacto(rs.getString("contacto"));
                p.setTelefono(rs.getString("telefono"));
                p.setCorreo(rs.getString("correo"));
                lista.add(p);
            }
        } catch (SQLException e) {
        }
        return lista;
    }

    public boolean actualizar(Proveedor proveedor) {
        String sql = "UPDATE proveedores SET nombre=?, contacto=?, telefono=?, correo=? WHERE id=?";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, proveedor.getNombre());
            pstmt.setString(2, proveedor.getContacto());
            pstmt.setString(3, proveedor.getTelefono());
            pstmt.setString(4, proveedor.getCorreo());
            pstmt.setString(5, proveedor.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(String id) {
        String sql = "DELETE FROM proveedores WHERE id=?";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}