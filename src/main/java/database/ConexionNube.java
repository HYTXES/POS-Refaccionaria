package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionNube {

    public static String SUCURSAL_ACTUAL = "Desconocida"; 

    // --- CONFIGURACIÓN PARA XAMPP LOCALHOST ---
    private static final String HOST = "localhost"; 
    private static final String PORT = "3306"; 
    private static final String DATABASE = "nuberef";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; 

    private static final String URL = "jdbc:mariadb://" + HOST + ":" + PORT + "/" + DATABASE 
                                    + "?autoReconnect=true&serverTimezone=UTC";

    // --- LÓGICA DE IDENTIDAD ---
    public static void cargarConfiguracionLocal() {
        String sqlCrear = "CREATE TABLE IF NOT EXISTS configuracion (clave VARCHAR(50) PRIMARY KEY, valor VARCHAR(100))";
        String sqlLeer = "SELECT valor FROM configuracion WHERE clave = 'nombre_sucursal'";

        try (Connection conn = ConexionLocal.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCrear);

            try (ResultSet rs = stmt.executeQuery(sqlLeer)) {
                if (rs.next()) {
                    SUCURSAL_ACTUAL = rs.getString("valor");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error leyendo la configuración local: " + e.getMessage());
        }
    }

    public static Connection conectar() {
        Connection conexion = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC de MariaDB.");
        } catch (SQLException e) {
            System.err.println(" Error conectando a la Nube (MariaDB): " + e.getMessage());
        }
        return conexion;
    }

    public static boolean probarConexion() {
        try (Connection conn = conectar()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}