package interfaces;

import entities.DetalleVenta;
import entities.Venta;
import java.util.List;

public interface IVentaRepository {
    
    boolean guardarVenta(Venta venta, List<DetalleVenta> detalles);
    
    List<Venta> obtenerTodas();
    
    List<Venta> obtenerNoSincronizadas();
    boolean marcarComoSincronizada(String id);
}