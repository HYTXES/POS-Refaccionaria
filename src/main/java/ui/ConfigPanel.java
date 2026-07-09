package ui;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class ConfigPanel extends JPanel {

    private JTextField txtNombreNegocio;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JTextArea txtMensajeTicket;
    private JButton btnGuardar;
    private Preferences prefs;

    public ConfigPanel() {
        prefs = Preferences.userRoot().node("RefaccionariaChayitoConfig");

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("Configuración del Sistema");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlCentro = new JPanel(new GridBagLayout());
        pnlCentro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNombre = new JLabel("Nombre del Negocio:");
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 14));
        pnlCentro.add(lblNombre, gbc);

        gbc.gridx = 1; 
        txtNombreNegocio = new JTextField(25);
        txtNombreNegocio.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pnlCentro.add(txtNombreNegocio, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblTel = new JLabel("Teléfono de Contacto:");
        lblTel.setFont(new Font("SansSerif", Font.BOLD, 14));
        pnlCentro.add(lblTel, gbc);

        gbc.gridx = 1; 
        txtTelefono = new JTextField(25);
        txtTelefono.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pnlCentro.add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblDir = new JLabel("Dirección Impresa:");
        lblDir.setFont(new Font("SansSerif", Font.BOLD, 14));
        pnlCentro.add(lblDir, gbc);

        gbc.gridx = 1; 
        txtDireccion = new JTextField(25);
        txtDireccion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pnlCentro.add(txtDireccion, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblMsg = new JLabel("Mensaje al pie del Ticket:");
        lblMsg.setFont(new Font("SansSerif", Font.BOLD, 14));
        pnlCentro.add(lblMsg, gbc);

        gbc.gridx = 1; 
        txtMensajeTicket = new JTextArea(3, 25);
        txtMensajeTicket.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtMensajeTicket.setLineWrap(true);
        pnlCentro.add(new JScrollPane(txtMensajeTicket), gbc);

        add(pnlCentro, BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSur.setOpaque(false);
        
        btnGuardar = new JButton("💾 Guardar Configuración");
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnGuardar.setBackground(new Color(51, 65, 85));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setPreferredSize(new Dimension(220, 45));
        pnlSur.add(btnGuardar);
        
        add(pnlSur, BorderLayout.SOUTH);

        cargarPreferencias();

        btnGuardar.addActionListener(e -> guardarPreferencias());
    }

    private void cargarPreferencias() {
        txtNombreNegocio.setText(prefs.get("nombreNegocio", "Refaccionaria Chayito"));
        txtTelefono.setText(prefs.get("telefono", ""));
        txtDireccion.setText(prefs.get("direccion", ""));
        txtMensajeTicket.setText(prefs.get("mensajeTicket", "¡Gracias por su compra, vuelva pronto!"));
    }

    private void guardarPreferencias() {
        prefs.put("nombreNegocio", txtNombreNegocio.getText().trim());
        prefs.put("telefono", txtTelefono.getText().trim());
        prefs.put("direccion", txtDireccion.getText().trim());
        prefs.put("mensajeTicket", txtMensajeTicket.getText().trim());
        
        JOptionPane.showMessageDialog(this, "Configuración guardada exitosamente.\nEstos datos aparecerán en los tickets de venta.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}