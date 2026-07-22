package repository;

import database.ConexionLocal;
import entities.Producto;
import interfaces.IProductoRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoLocalImpl implements IProductoRepository {

    public ProductoLocalImpl() {
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS productos ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "codigo_barras VARCHAR(50) UNIQUE NOT NULL, "
                + "numero_parte VARCHAR(50), "
                + "nombre VARCHAR(100) NOT NULL, "
                + "marca VARCHAR(50), "
                + "vehiculos_compatibles TEXT, "
                + "precio_compra DECIMAL(10,2) NOT NULL, "
                + "precio_venta DECIMAL(10,2) NOT NULL, "
                + "stock_actual INTEGER DEFAULT 0, "
                + "stock_critico INTEGER DEFAULT 0, "
                + "proveedor VARCHAR(100), "
                + "sincronizado INTEGER DEFAULT 0)";

        try (Connection conn = ConexionLocal.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
        }
    }

    @Override
    public boolean guardar(Producto producto) {
        String sql = "INSERT INTO productos(id, codigo_barras, numero_parte, nombre, marca, "
                + "vehiculos_compatibles, precio_compra, precio_venta, stock_actual, stock_critico, sincronizado) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = ConexionLocal.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getId());
            pstmt.setString(2, producto.getCodigoBarras());
            pstmt.setString(3, producto.getNumeroParte());
            pstmt.setString(4, producto.getNombre());
            pstmt.setString(5, producto.getMarca());
            pstmt.setString(6, producto.getVehiculosCompatibles());
            pstmt.setDouble(7, producto.getPrecioCompra());
            pstmt.setDouble(8, producto.getPrecioVenta());
            pstmt.setInt(9, producto.getStockActual());
            pstmt.setInt(10, producto.getStockCritico());
            pstmt.setInt(11, producto.isSincronizado() ? 1 : 0);
            
            pstmt.executeUpdate();
            
            if (producto.getStockActual() > 0) {
                DashboardLocalImpl dashRepo = new DashboardLocalImpl();
                dashRepo.registrarEntradaMercancia(producto.getCodigoBarras(), producto.getStockActual());
            }
            
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";

        try (Connection conn = ConexionLocal.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto(rs.getString("id"));
                p.setCodigoBarras(rs.getString("codigo_barras"));
                p.setNumeroParte(rs.getString("numero_parte"));
                p.setNombre(rs.getString("nombre"));
                p.setMarca(rs.getString("marca"));
                p.setVehiculosCompatibles(rs.getString("vehiculos_compatibles"));
                p.setPrecioCompra(rs.getDouble("precio_compra"));
                p.setPrecioVenta(rs.getDouble("precio_venta"));
                p.setStockActual(rs.getInt("stock_actual"));
                p.setStockCritico(rs.getInt("stock_critico"));
                p.setSincronizado(rs.getInt("sincronizado") == 1);
                lista.add(p);
            }
        } catch (SQLException e) {
        }
        return lista;
    }

    @Override
    public Producto buscarPorCodigo(String codigoBarras) {
        String sql = "SELECT * FROM productos WHERE codigo_barras = ?";
        try (Connection conn = ConexionLocal.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoBarras);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto(rs.getString("id"));
                    p.setCodigoBarras(rs.getString("codigo_barras"));
                    p.setNumeroParte(rs.getString("numero_parte"));
                    p.setNombre(rs.getString("nombre"));
                    p.setMarca(rs.getString("marca"));
                    p.setVehiculosCompatibles(rs.getString("vehiculos_compatibles"));
                    p.setPrecioCompra(rs.getDouble("precio_compra"));
                    p.setPrecioVenta(rs.getDouble("precio_venta"));
                    p.setStockActual(rs.getInt("stock_actual"));
                    p.setStockCritico(rs.getInt("stock_critico"));
                    return p;
                }
            }
        } catch (SQLException e) {
        }
        return null;
    }

    @Override
    public boolean actualizar(Producto producto) {
        
        Producto productoAnterior = buscarPorCodigo(producto.getCodigoBarras());
        if (productoAnterior != null) {
            int diferenciaStock = producto.getStockActual() - productoAnterior.getStockActual();
            
            if (diferenciaStock > 0) {
                DashboardLocalImpl dashRepo = new DashboardLocalImpl();
                dashRepo.registrarEntradaMercancia(producto.getCodigoBarras(), diferenciaStock);
            }
        }
        // -----------------------------------------------------------------------

        String sql = "UPDATE productos SET nombre=?, marca=?, precio_compra=?, precio_venta=?, stock_actual=?, stock_critico=?, vehiculos_compatibles=?, numero_parte=?, sincronizado = 0 WHERE codigo_barras=?";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getMarca());
            pstmt.setDouble(3, producto.getPrecioCompra());
            pstmt.setDouble(4, producto.getPrecioVenta());
            pstmt.setInt(5, producto.getStockActual());
            pstmt.setInt(6, producto.getStockCritico());
            pstmt.setString(7, producto.getVehiculosCompatibles());
            pstmt.setString(8, producto.getNumeroParte());
            pstmt.setString(9, producto.getCodigoBarras());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(String codigoBarras) {
        String sql = "DELETE FROM productos WHERE codigo_barras=?";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoBarras);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Producto> buscarPorFiltro(String filtro) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE codigo_barras LIKE ? OR nombre LIKE ? OR marca LIKE ? LIMIT 15";
        try (Connection conn = ConexionLocal.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String parametro = "%" + filtro + "%";
            pstmt.setString(1, parametro);
            pstmt.setString(2, parametro);
            pstmt.setString(3, parametro);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto(rs.getString("id"));
                    p.setCodigoBarras(rs.getString("codigo_barras"));
                    p.setNombre(rs.getString("nombre"));
                    p.setMarca(rs.getString("marca"));
                    p.setPrecioVenta(rs.getDouble("precio_venta"));
                    p.setStockActual(rs.getInt("stock_actual"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
        }
        return lista;
    }

    @Override public List<Producto> buscarPorNumeroParte(String numeroParte) { return null; }
    @Override public List<Producto> obtenerNoSincronizados() { return null; }
    @Override public boolean marcarComoSincronizado(String id) { return false; }
}