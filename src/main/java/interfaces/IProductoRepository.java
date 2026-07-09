package interfaces;

import entities.Producto;
import java.util.List;

public interface IProductoRepository {
    
    boolean guardar(Producto producto);
    
    Producto buscarPorCodigo(String codigoBarras);
    
    List<Producto> buscarPorNumeroParte(String numeroParte);
    
    List<Producto> obtenerTodos();
    
    boolean actualizar(Producto producto);
    
    List<Producto> obtenerNoSincronizados();
    
    boolean marcarComoSincronizado(String id);
}