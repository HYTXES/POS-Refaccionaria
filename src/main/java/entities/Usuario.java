package entities;

import java.util.UUID;

public class Usuario {

    private String id;
    private String username;
    private String password;
    private String rol;

    public Usuario() {
        this.id = UUID.randomUUID().toString();
    }

    public Usuario(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}