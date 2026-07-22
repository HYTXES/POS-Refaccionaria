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

    public static void auditarAcceso(JFrame ventana, String rolRequerido) {
        if (usuarioActual == null) {
            bloquearYExpulsar(ventana, "Acceso denegado: No hay una sesión activa.");
            return;
        }

        if (!rolRequerido.equals("CUALQUIERA") && !usuarioActual.getRol().equalsIgnoreCase(rolRequerido)) {
            bloquearYExpulsar(ventana, "Violación de seguridad: Tu rol (" + usuarioActual.getRol() + ") no tiene permisos para esta área.");
        }
    }

    private static void bloquearYExpulsar(JFrame ventana, String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, "Alerta de Seguridad", JOptionPane.ERROR_MESSAGE);
        ventana.dispose();
        new Login().setVisible(true);
    }
}