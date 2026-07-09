package ui;

import entities.Sesion;
import repository.DashboardLocalImpl;
import javax.swing.*;
import java.awt.*;

public class DialogoRetiro extends JDialog {

    public DialogoRetiro(JFrame parent) {
        super(parent, "Registrar Retiro de Caja", true);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel pnlCentro = new JPanel(new GridLayout(4, 1, 10, 10));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        pnlCentro.add(new JLabel("Monto a retirar ($):"));
        JTextField txtMonto = new JTextField();
        txtMonto.setFont(new Font("SansSerif", Font.BOLD, 18));
        pnlCentro.add(txtMonto);

        pnlCentro.add(new JLabel("Motivo del retiro:"));
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

                String vendedor = (Sesion.getUsuarioActual() != null) ? Sesion.getUsuarioActual().getUsername() : "Usuario";
                if (new DashboardLocalImpl().registrarRetiro(vendedor, monto, motivo)) {
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