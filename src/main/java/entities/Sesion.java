package entities;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import ui.Login;

public class Sesion {
    private static Usuario usuarioActual;

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    // --- NUEVO: AUDITOR DE SEGURIDAD GLOBAL ---
    public static void auditarAcceso(JFrame ventana, String rolRequerido) {
        // Verifica si alguien intentó entrar sin iniciar sesión
        if (usuarioActual == null) {
            bloquearYExpulsar(ventana, "Acceso denegado: No hay una sesión activa.");
            return;
        }

        // Verifica si el usuario tiene el nivel de privilegios correcto
        if (!rolRequerido.equals("CUALQUIERA") && !usuarioActual.getRol().equalsIgnoreCase(rolRequerido)) {
            bloquearYExpulsar(ventana, "Violación de seguridad: Tu rol (" + usuarioActual.getRol() + ") no tiene permisos para esta área.");
        }
    }

    private static void bloquearYExpulsar(JFrame ventana, String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, "Alerta de Seguridad", JOptionPane.ERROR_MESSAGE);
        ventana.dispose(); // Destruye la ventana
        new Login().setVisible(true); // Lo regresa al login
    }
}