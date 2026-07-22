package sync;

import database.ConexionLocal;
import database.ConexionNube;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SincronizadorNube {

    private ScheduledExecutorService scheduler;
    private final int INTERVALO_MINUTOS = 1; // 1 min

    public void iniciar() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::sincronizarDatos, 0, INTERVALO_MINUTOS, TimeUnit.MINUTES);
            System.out.println("Motor de Sincronización INICIADO (Modo Inventario Global).");
        }
    }

    public void detener() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("Motor de Sincronización DETENIDO.");
        }
    }

    private void sincronizarDatos() {
        if (!ConexionNube.probarConexion()) {
            System.out.println("Nube inaccesible. Reintentando en " + INTERVALO_MINUTOS + " min...");
            return; 
        }

        sincronizarInventario();
    }

    // --- SINCRONIZAR EXCLUSIVAMENTE EL INVENTARIO ---
    private void sincronizarInventario() {
        String sqlSelectLocal = "SELECT codigo_barras, nombre, stock_actual FROM productos WHERE sincronizado = 0";
        String sqlUpdateLocal = "UPDATE productos SET sincronizado = 1 WHERE codigo_barras = ?";
        String sqlUpsertNube = "INSERT INTO inventario_global (codigo_barras, nombre, sucursal, stock_actual) "
                             + "VALUES (?, ?, ?, ?) "
                             + "ON DUPLICATE KEY UPDATE "
                             + "nombre = VALUES(nombre), stock_actual = VALUES(stock_actual)";

        try (Connection connLocal = ConexionLocal.conectar();
             Statement stmtLocal = connLocal.createStatement();
             ResultSet rsPendientes = stmtLocal.executeQuery(sqlSelectLocal)) {

            PreparedStatement pstUpdateLocal = connLocal.prepareStatement(sqlUpdateLocal);
            
            try (Connection connNube = ConexionNube.conectar();
                 PreparedStatement pstUpsertNube = connNube.prepareStatement(sqlUpsertNube)) {

                int registrosSubidos = 0;

                while (rsPendientes.next()) {
                    String codigoBarras = rsPendientes.getString("codigo_barras");

                    try {
                        pstUpsertNube.setString(1, codigoBarras);
                        pstUpsertNube.setString(2, rsPendientes.getString("nombre"));
                        pstUpsertNube.setString(3, ConexionNube.SUCURSAL_ACTUAL);
                        pstUpsertNube.setInt(4, rsPendientes.getInt("stock_actual"));
                        
                        pstUpsertNube.executeUpdate(); 

                        pstUpdateLocal.setString(1, codigoBarras);
                        pstUpdateLocal.executeUpdate();
                        
                        registrosSubidos++;

                    } catch (SQLException ex) {
                        System.out.println("Error al subir el producto " + codigoBarras + ": " + ex.getMessage());
                    }
                }

                if (registrosSubidos > 0) {
                    System.out.println(registrosSubidos + " productos sincronizados en la nube como: " + ConexionNube.SUCURSAL_ACTUAL);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error leyendo SQLite local (Productos): " + e.getMessage());
        }
    }
}