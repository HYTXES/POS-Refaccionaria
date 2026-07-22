package repository;

import database.ConexionLocal;
import entities.Usuario;
import util.SeguridadUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioLocalImpl {

    public UsuarioLocalImpl() {
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "username VARCHAR(50) UNIQUE NOT NULL, "
                + "password VARCHAR(255) NOT NULL, "
                + "rol VARCHAR(20) NOT NULL)";

        try (Connection conn = ConexionLocal.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            verificarAdminInicial(conn);
        } catch (SQLException e) {
        }
    }

    private void verificarAdminInicial(Connection conn) {
        String sqlCheck = "SELECT COUNT(*) AS total FROM usuarios";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlCheck)) {
            if (rs.next() && rs.getInt("total") == 0) {
                String passHasheada = SeguridadUtil.hashPassword("admin123");
                
                String sqlInsert = "INSERT INTO usuarios(id, username, password, rol) VALUES(?, 'admin', ?, 'Administrador')";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    pstmt.setString(1, java.util.UUID.randomUUID().toString());
                    pstmt.setString(2, passHasheada);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
        }
    }

    public boolean guardar(Usuario u) {
        String passHasheada = SeguridadUtil.hashPassword(u.getPassword());
        
        String sql = "INSERT INTO usuarios(id, username, password, rol) VALUES(?,?,?,?)";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getId());
            pstmt.setString(2, u.getUsername());
            pstmt.setString(3, passHasheada);
            pstmt.setString(4, u.getRol());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Usuario> obtenerTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, username, rol FROM usuarios";
        try (Connection conn = ConexionLocal.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Usuario u = new Usuario(rs.getString("id"));
                u.setUsername(rs.getString("username"));
                u.setRol(rs.getString("rol"));
                lista.add(u);
            }
        } catch (SQLException e) {
        }
        return lista;
    }

    public boolean actualizarPassword(String id, String nuevaPasswordPlana) {
        String passHasheada = SeguridadUtil.hashPassword(nuevaPasswordPlana);
        
        String sql = "UPDATE usuarios SET password=? WHERE id=?";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, passHasheada); 
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(String id) {
        String sql = "DELETE FROM usuarios WHERE id=?";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public Usuario autenticar(String username, String passwordPlana) {
        String intentoHash = SeguridadUtil.hashPassword(passwordPlana);
        
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, intentoHash);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(rs.getString("id"));
                    u.setUsername(rs.getString("username"));
                    u.setRol(rs.getString("rol"));
                    return u;
                }
            }
        } catch (SQLException e) {
        }
        return null;
    }
}