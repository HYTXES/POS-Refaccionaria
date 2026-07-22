package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ConfiguracionGlobal {

    public static String getNombreEmpresaTicket() {
        String porDefecto = "Refaccionaria Chayito";
        String sql = "SELECT valor FROM configuracion WHERE clave = 'empresa_nombre'";
        try (Connection conn = database.ConexionLocal.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String val = rs.getString("valor");
                if (val != null && !val.trim().isEmpty()) {
                    return val;
                }
            }
        } catch (Exception e) {}
        return porDefecto;
    }
    
    public static String getNombreSucursal() {
        String nombrePorDefecto = "Refaccionaria Chayito";
        String sql = "SELECT valor FROM configuracion WHERE clave = 'nombre_sucursal'";
        try (Connection conn = database.ConexionLocal.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String val = rs.getString("valor");
                if (val != null && !val.trim().isEmpty()) {
                    return val;
                }
            }
        } catch (Exception e) {}
        return nombrePorDefecto;
    }

    public static void setNombreSucursal(String nuevoNombre) {
        guardarClaveValor("nombre_sucursal", nuevoNombre);
    }

    public static String getTicketCabecera() {
        String porDefecto = "Las mejores refacciones al mejor precio";
        String sql = "SELECT valor FROM configuracion WHERE clave = 'ticket_cabecera'";
        try (Connection conn = database.ConexionLocal.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String val = rs.getString("valor");
                if (val != null && !val.trim().isEmpty()) {
                    return val;
                }
            }
        } catch (Exception e) {}
        return porDefecto;
    }

    public static void setTicketCabecera(String mensaje) {
        guardarClaveValor("ticket_cabecera", mensaje);
    }

    public static String getTicketPie() {
        String porDefecto = "¡Gracias por su preferencia! Vuelva pronto.";
        String sql = "SELECT valor FROM configuracion WHERE clave = 'ticket_pie'";
        try (Connection conn = database.ConexionLocal.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String val = rs.getString("valor");
                if (val != null && !val.trim().isEmpty()) {
                    return val;
                }
            }
        } catch (Exception e) {}
        return porDefecto;
    }

    public static void setTicketPie(String mensaje) {
        guardarClaveValor("ticket_pie", mensaje);
    }

    private static void guardarClaveValor(String clave, String valor) {
        String sqlVerificar = "SELECT COUNT(*) FROM configuracion WHERE clave = ?";
        String sqlInsert = "INSERT INTO configuracion (clave, valor) VALUES (?, ?)";
        String sqlUpdate = "UPDATE configuracion SET valor = ? WHERE clave = ?";

        try (Connection conn = database.ConexionLocal.conectar()) {
            boolean existe = false;
            try (PreparedStatement pst = conn.prepareStatement(sqlVerificar)) {
                pst.setString(1, clave);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        existe = true;
                    }
                }
            }

            if (existe) {
                try (PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {
                    pst.setString(1, valor);
                    pst.setString(2, clave);
                    pst.executeUpdate();
                }
            } else {
                try (PreparedStatement pst = conn.prepareStatement(sqlInsert)) {
                    pst.setString(1, clave);
                    pst.setString(2, valor);
                    pst.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Error al guardar configuración [" + clave + "]: " + e.getMessage());
        }
    }
}