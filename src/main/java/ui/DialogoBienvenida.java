package ui;

import util.ConfiguracionGlobal;
import javax.swing.*;
import java.awt.*;

public class DialogoBienvenida extends JDialog {
    
    private JTextField txtNombre;
    private boolean configurado = false;

    public DialogoBienvenida() {
        setTitle("Configuración Inicial");
        setSize(450, 250);
        setLocationRelativeTo(null);
        setModal(true);
        setUndecorated(true);

        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgPanel = isDark ? new Color(30, 41, 59) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");

        JPanel pnlFondo = new JPanel(new BorderLayout(15, 15));
        pnlFondo.setBackground(bgPanel);
        // - Borde azul -
        pnlFondo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#0F43AB"), 2),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        // --- ZONA CENTRAL: TEXTOS Y CAMPO ---
        JPanel pnlCentro = new JPanel(new GridLayout(3, 1, 10, 10));
        pnlCentro.setOpaque(false);

        JLabel lblTitulo = new JLabel("👋 ¡Bienvenido al Sistema!", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(txtPrincipal);

        JLabel lblSub = new JLabel("Ingresa el nombre de tu negocio o sucursal:", SwingConstants.CENTER);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSub.setForeground(isDark ? new Color(148, 163, 184) : new Color(100, 116, 139));

        txtNombre = new JTextField();
        txtNombre.setFont(new Font("SansSerif", Font.BOLD, 18));
        txtNombre.setHorizontalAlignment(JTextField.CENTER);

        pnlCentro.add(lblTitulo);
        pnlCentro.add(lblSub);
        pnlCentro.add(txtNombre);

        pnlFondo.add(pnlCentro, BorderLayout.CENTER);

        // --- ZONA INFERIOR: BOTÓN ---
        JButton btnGuardar = new JButton("Comenzar 🚀");
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnGuardar.setBackground(Color.decode("#0F43AB"));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnGuardar.addActionListener(e -> guardarYContinuar());

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlSur.setOpaque(false);
        pnlSur.add(btnGuardar);
        
        pnlFondo.add(pnlSur, BorderLayout.SOUTH);
        setContentPane(pnlFondo);
    }

    private void guardarYContinuar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ConfiguracionGlobal.setNombreSucursal(nombre);
        configurado = true;
        dispose();
    }

    public boolean isConfigurado() {
        return configurado;
    }
}