package entities;

import java.util.UUID;

public class Producto {
    private String id;
    private String codigoBarras;
    private String numeroParte;
    private String nombre;
    private String marca;
    private String vehiculosCompatibles;
    private double precioCompra;
    private double precioVenta;
    private int stockActual;
    private boolean sincronizado;
    private int stockCritico;

    public Producto() {
        this.id = UUID.randomUUID().toString();
        this.sincronizado = false;
    }

    public Producto(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNumeroParte() { return numeroParte; }
    public void setNumeroParte(String numeroParte) { this.numeroParte = numeroParte; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getVehiculosCompatibles() { return vehiculosCompatibles; }
    public void setVehiculosCompatibles(String vehiculosCompatibles) { this.vehiculosCompatibles = vehiculosCompatibles; }

    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
    
    public int getStockCritico() { return stockCritico; }
    public void setStockCritico(int stockCritico) { this.stockCritico = stockCritico; }
}