package entities;

import java.util.UUID;

public class DetalleVenta {
    private String id;
    private String ventaId;
    private String productoId;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private boolean sincronizado;

    public DetalleVenta() {
        this.id = UUID.randomUUID().toString();
        this.sincronizado = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVentaId() { return ventaId; }
    public void setVentaId(String ventaId) { this.ventaId = ventaId; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}