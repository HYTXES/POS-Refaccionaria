package ui;

import database.ConexionLocal;
import database.ConexionNube;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ConfiguracionPanel extends JPanel {

    private Map<String, JTextField> camposConfiguracion;
    private JComboBox<String> cmbTema;

    // --- VARIABLES DE PALETA DINÁMICA ---
    private Color bgFondo;
    private Color bgTarjetas;
    private Color txtPrincipal;
    private Color txtSecundario;
    private Color colorBordes;

    public ConfiguracionPanel() {
        camposConfiguracion = new HashMap<>();
        
        // 1. DETECTAR EL TEMA ACTUAL
        boolean isDark = UIManager.getBoolean("laf.dark");

        // 2. CONFIGURAR LA PALETA SEGÚN EL TEMA
        bgFondo = isDark ? new Color(15, 23, 42) : Color.decode("#F8FAFC");        // Fondo general
        bgTarjetas = isDark ? new Color(30, 41, 59) : Color.WHITE;                  // Color de las tarjetas
        txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");              // Títulos
        txtSecundario = isDark ? new Color(148, 163, 184) : new Color(100, 116, 139); // Textos de apoyo
        colorBordes = isDark ? new Color(51, 65, 85) : new Color(226, 232, 240);    // Líneas separadoras
        
        setLayout(new BorderLayout());
        setBackground(bgFondo);

        // --- CABECERA ---
        JPanel pnlCabecera = new JPanel(new GridLayout(2, 1));
        pnlCabecera.setBackground(bgTarjetas);
        pnlCabecera.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, colorBordes),
            new EmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel lblTitulo = new JLabel("⚙️ Configuración Global del Sistema");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(txtPrincipal); // Color dinámico
        
        JLabel lblSub = new JLabel("Modifica los parámetros de red, empresa, tickets y preferencias.");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSub.setForeground(txtSecundario); // Color dinámico
        
        pnlCabecera.add(lblTitulo);
        pnlCabecera.add(lblSub);

        // --- CONTENIDO CENTRAL (CON SCROLL) ---
        JPanel pnlContenido = new JPanel();
        pnlContenido.setLayout(new BoxLayout(pnlContenido, BoxLayout.Y_AXIS));
        pnlContenido.setOpaque(false);
        pnlContenido.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Tarjeta 1: Nube y Red
        JPanel pnlNube = crearTarjeta("Conexión a la Nube (Base de datos global)");
        agregarCampo(pnlNube, "Nombre de esta Sucursal:", "nombre_sucursal");
        agregarCampo(pnlNube, "Dirección IP del Servidor:", "nube_ip");
        agregarCampo(pnlNube, "Puerto (Ej. 3306):", "nube_puerto");
        agregarCampo(pnlNube, "Usuario de la Nube:", "nube_usuario");
        agregarCampo(pnlNube, "Contraseña de la Nube:", "nube_password");
        pnlContenido.add(pnlNube);
        pnlContenido.add(Box.createRigidArea(new Dimension(0, 20)));

        // Tarjeta 2: Datos de Empresa (Para el Ticket)
        JPanel pnlEmpresa = crearTarjeta("Datos del Ticket de Venta");
        agregarCampo(pnlEmpresa, "Nombre del Negocio (Ticket):", "empresa_nombre");
        agregarCampo(pnlEmpresa, "RFC de la Empresa:", "empresa_rfc");
        agregarCampo(pnlEmpresa, "Dirección del Local:", "empresa_direccion");
        agregarCampo(pnlEmpresa, "Teléfono / WhatsApp:", "empresa_telefono");
        agregarCampo(pnlEmpresa, "Lema del Ticket (Cabecera):", "ticket_cabecera");
        agregarCampo(pnlEmpresa, "Mensaje de Despedida (Pie):", "ticket_pie");
        pnlContenido.add(pnlEmpresa);
        pnlContenido.add(Box.createRigidArea(new Dimension(0, 20)));

        // Tarjeta 3: Preferencias Operativas
        JPanel pnlPreferencias = crearTarjeta("Preferencias del Sistema");
        agregarCampo(pnlPreferencias, "Fondo de Caja Sugerido ($):", "pref_fondo_caja");
        agregarCampo(pnlPreferencias, "Alerta de Stock Mínimo (Uds):", "pref_stock_min");
        agregarCampo(pnlPreferencias, "Impuesto IVA Predeterminado (%):", "pref_iva");
        
        // Selector de Tema Visual
        JPanel pnlTema = new JPanel(new BorderLayout(10, 0));
        pnlTema.setOpaque(false);
        JLabel lblTema = new JLabel("Apariencia:");
        lblTema.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTema.setForeground(txtSecundario); // Color dinámico
        
        cmbTema = new JComboBox<>(new String[]{"Claro (Light)", "Oscuro (Dark)"});
        cmbTema.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pnlTema.add(lblTema, BorderLayout.WEST);
        pnlTema.add(cmbTema, BorderLayout.CENTER);
        pnlPreferencias.add(pnlTema);
        
        pnlContenido.add(pnlPreferencias);

        JScrollPane scrollPane = new JScrollPane(pnlContenido);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // --- PIE DE PÁGINA (BOTÓN GUARDAR) ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFooter.setBackground(bgTarjetas);
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, colorBordes),
            new EmptyBorder(15, 30, 15, 30)
        ));
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnGuardar.setBackground(Color.decode("#0F43AB"));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setPreferredSize(new Dimension(100, 45));
        
        pnlFooter.add(btnGuardar);

        add(pnlCabecera, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);

        cargarDatosActuales();
        btnGuardar.addActionListener(e -> guardarConfiguracion());
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 20, 15)); 
        panel.setBackground(bgTarjetas); 
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorBordes, 1),
            BorderFactory.createTitledBorder(
                new EmptyBorder(10, 10, 10, 10), 
                titulo, 
                0, 0, 
                new Font("SansSerif", Font.BOLD, 16), 
                Color.decode("#3B82F6") 
            )
        ));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(10, 15, 15, 15));
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    private void agregarCampo(JPanel tarjeta, String etiqueta, String claveBD) {
        JPanel pnlCampo = new JPanel(new BorderLayout(5, 5));
        pnlCampo.setOpaque(false);
        
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(txtSecundario); 
        
        JTextField txt = new JTextField();
        txt.setFont(new Font("SansSerif", Font.PLAIN, 15));
        
        pnlCampo.add(lbl, BorderLayout.NORTH);
        pnlCampo.add(txt, BorderLayout.CENTER);
        
        ((JPanel) tarjeta.getComponent(0)).add(pnlCampo);
        
        camposConfiguracion.put(claveBD, txt);
    }

    private void cargarDatosActuales() {
        String sql = "SELECT clave, valor FROM configuracion";
        try (Connection conn = ConexionLocal.conectar(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                String clave = rs.getString("clave");
                String valor = rs.getString("valor");
                
                if (camposConfiguracion.containsKey(clave)) {
                    camposConfiguracion.get(clave).setText(valor);
                }
                
                if (clave.equals("tema_oscuro")) {
                    cmbTema.setSelectedIndex(valor.equals("true") ? 1 : 0);
                }
            }
        } catch (Exception e) {}
    }

    private void guardarConfiguracion() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Guardar todos los cambios de configuración?", "Confirmar Guardado", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = ConexionLocal.conectar()) {
            conn.setAutoCommit(false); 
            for (Map.Entry<String, JTextField> entry : camposConfiguracion.entrySet()) {
                guardarClaveValor(conn, entry.getKey(), entry.getValue().getText().trim());
            }
            guardarClaveValor(conn, "tema_oscuro", cmbTema.getSelectedIndex() == 1 ? "true" : "false");
            conn.commit(); 
            ConexionNube.SUCURSAL_ACTUAL = camposConfiguracion.get("nombre_sucursal").getText().trim();
            JOptionPane.showMessageDialog(this, "Configuración actualizada con éxito.\n(El cambio de tema requiere reiniciar el programa)", "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarClaveValor(Connection conn, String clave, String valor) throws Exception {
        String sqlUpdate = "UPDATE configuracion SET valor = ? WHERE clave = ?";
        try (PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {
            pst.setString(1, valor);
            pst.setString(2, clave);
            if (pst.executeUpdate() == 0) {
                String sqlInsert = "INSERT INTO configuracion (clave, valor) VALUES (?, ?)";
                try (PreparedStatement pstInsert = conn.prepareStatement(sqlInsert)) {
                    pstInsert.setString(1, clave);
                    pstInsert.setString(2, valor);
                    pstInsert.executeUpdate();
                }
            }
        }
    }
}