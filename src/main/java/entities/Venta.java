package entities;

import java.util.Date;
import java.util.UUID;

public class Venta {
    private String id;
    private String folio;
    private Date fechaVenta;
    private double total;
    private String metodoPago;
    private String estado;
    private boolean sincronizado;
    private String vendedor;

    public Venta() {
        this.id = UUID.randomUUID().toString();
        this.fechaVenta = new Date();
        this.estado = "COMPLETADA";
        this.sincronizado = false;
    }

    public Venta(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public Date getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(Date fechaVenta) { this.fechaVenta = fechaVenta; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }
}