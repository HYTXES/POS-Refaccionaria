package ui;

import entities.Sesion;
import repository.CajaLocalImpl;
import repository.DashboardLocalImpl;
import javax.swing.*;
import java.awt.*;

public class DialogoRetiro extends JDialog {

    public DialogoRetiro(JFrame parent) {
        super(parent, "Registrar Retiro de Caja", true);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgTarjetas = isDark ? new Color(30, 41, 59) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");

        getContentPane().setBackground(bgTarjetas);

        JPanel pnlCentro = new JPanel(new GridLayout(4, 1, 10, 10));
        pnlCentro.setOpaque(false);
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblMonto = new JLabel("Monto a retirar ($):");
        lblMonto.setForeground(txtPrincipal);
        pnlCentro.add(lblMonto);

        JTextField txtMonto = new JTextField();
        txtMonto.setFont(new Font("SansSerif", Font.BOLD, 18));
        pnlCentro.add(txtMonto);

        JLabel lblMotivo = new JLabel("Motivo del retiro:");
        lblMotivo.setForeground(txtPrincipal);
        pnlCentro.add(lblMotivo);

        JTextField txtMotivo = new JTextField();
        pnlCentro.add(txtMotivo);

        add(pnlCentro, BorderLayout.CENTER);

        JButton btnGuardar = new JButton("Confirmar Retiro");
        btnGuardar.setBackground(new Color(220, 38, 38)); // Rojo
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnGuardar.setFocusable(false);

        btnGuardar.addActionListener(e -> {
            try {
                double monto = Double.parseDouble(txtMonto.getText().trim());
                String motivo = txtMotivo.getText().trim();
                
                if (monto <= 0 || motivo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ingresa un monto válido y un motivo.");
                    return;
                }

                CajaLocalImpl cajaRepo = new CajaLocalImpl();
                DashboardLocalImpl dashRepo = new DashboardLocalImpl();

                double fondo = cajaRepo.obtenerFondoInicial();
                double ventas = cajaRepo.calcularVentasTurno();
                String fechaApertura = cajaRepo.obtenerFechaAperturaActual();
                double retirosPrevios = dashRepo.calcularRetirosTurnoActual(fechaApertura);

                double efectivoDisponible = (fondo + ventas) - retirosPrevios;

                if (monto > efectivoDisponible) {
                    JOptionPane.showMessageDialog(this, 
                        String.format("Operación denegada: Fondos insuficientes en caja.\nEfectivo físico disponible máximo: $%.2f", efectivoDisponible), 
                        "Error de Retiro", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String vendedor = (Sesion.getUsuarioActual() != null) ? Sesion.getUsuarioActual().getUsername() : "Usuario";
                
                if (dashRepo.registrarRetiro(vendedor, monto, motivo)) {
                    JOptionPane.showMessageDialog(this, "Retiro registrado. Guarda el comprobante físico en la caja.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Monto inválido.");
            }
        });

        add(btnGuardar, BorderLayout.SOUTH);
    }
}