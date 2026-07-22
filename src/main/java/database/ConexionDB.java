package database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDB {
    
    private static String urlDB;

    static {
        cargarConfiguracion();
    }

    private static void cargarConfiguracion() {
        Properties prop = new Properties();
        try (InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("config.properties")) {
            
            if (input == null) {
                System.err.println("Advertencia: No se encontra config.properties. Se usara la ruta por defecto.");
                urlDB = "jdbc:sqlite:refaccionaria_local.db";
                return;
            }
            
            prop.load(input);
            urlDB = prop.getProperty("db.local.url");
            
        } catch (Exception ex) {
            System.err.println("Error al leer la configuración de la BD: " + ex.getMessage());
            urlDB = "jdbc:sqlite:refaccionaria_local.db";
        }
    }

    public static Connection conectar() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(urlDB);
        } catch (SQLException e) {
            System.err.println("Error crítico al conectar a la base de datos: " + e.getMessage());
        }
        return conexion;
    }
}