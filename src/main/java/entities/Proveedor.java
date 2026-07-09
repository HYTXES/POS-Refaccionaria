package entities;

import java.util.UUID;

public class Proveedor {
    private String id;
    private String nombre;
    private String contacto;
    private String telefono;
    private String correo;

    public Proveedor() {
        this.id = UUID.randomUUID().toString();
    }

    public Proveedor(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    @Override
    public String toString() {
        return nombre;
    }
}