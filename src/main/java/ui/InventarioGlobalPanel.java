package ui;

import database.ConexionNube;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventarioGlobalPanel extends JPanel {

    private JTable tablaGlobal;
    private DefaultTableModel modeloGlobal;

    public InventarioGlobalPanel() {
        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgFondo = isDark ? new Color(15, 23, 42) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");

        setLayout(new BorderLayout(10, 10));
        setBackground(bgFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TÍTULO ---
        JLabel lblTitulo = new JLabel("Inventario Global - Nube");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(txtPrincipal); // Color dinámico
        add(lblTitulo, BorderLayout.NORTH);

        // --- TABLA PRINCIPAL ---
        modeloGlobal = new DefaultTableModel(new String[]{"Código de Barras", "Producto", "Stock Total (Todas las Sucursales)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaGlobal = new JTable(modeloGlobal);
        tablaGlobal.setRowHeight(30);
        tablaGlobal.setFont(new Font("SansSerif", Font.PLAIN, 15));
        tablaGlobal.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tablaGlobal.getTableHeader().setReorderingAllowed(false);
        tablaGlobal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(tablaGlobal), BorderLayout.CENTER);

        // --- PANEL DE ACCIONES ---
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlAcciones.setOpaque(false);
        
        JButton btnActualizar = new JButton("🔄 Refrescar Nube");
        JButton btnDetalles = new JButton("🔍 Ver Stock por Sucursal");
        
        // Estilo rápido a los botones
        Font fuenteBotones = new Font("SansSerif", Font.BOLD, 14);
        btnActualizar.setFont(fuenteBotones);
        btnDetalles.setFont(fuenteBotones);

        pnlAcciones.add(btnActualizar);
        pnlAcciones.add(btnDetalles);
        add(pnlAcciones, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnActualizar.addActionListener(e -> cargarDatosGlobales());

        btnDetalles.addActionListener(e -> {
            int fila = tablaGlobal.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String codigo = tablaGlobal.getValueAt(fila, 0).toString();
            String nombre = tablaGlobal.getValueAt(fila, 1).toString();
            mostrarDetallesSucursal(codigo, nombre, isDark);
        });

        // Cargar datos al mostrar el panel por primera vez
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                cargarDatosGlobales();
            }
        });
    }

    private void cargarDatosGlobales() {
        modeloGlobal.setRowCount(0); // Limpiar tabla
        
        String sql = "SELECT codigo_barras, nombre, SUM(stock_actual) as stock_total " +
                     "FROM inventario_global GROUP BY codigo_barras, nombre ORDER BY nombre ASC";

        try (Connection conn = ConexionNube.conectar()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la Nube. Revisa tu internet.", "Modo Offline", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement pst = conn.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                 
                while (rs.next()) {
                    modeloGlobal.addRow(new Object[]{
                        rs.getString("codigo_barras"),
                        rs.getString("nombre"),
                        rs.getInt("stock_total") + " piezas"
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al consultar la nube: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDetallesSucursal(String codigo, String nombreProducto, boolean isDark) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalles de Stock", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        
        Color bgDialog = isDark ? new Color(30, 41, 59) : Color.WHITE;
        Color txtDialog = isDark ? Color.WHITE : Color.decode("#1E293B");
        
        dialog.getContentPane().setBackground(bgDialog);

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblProd = new JLabel("<html><b>Producto:</b> " + nombreProducto + "</html>");
        lblProd.setForeground(txtDialog);
        JLabel lblCod = new JLabel("<html><b>Código:</b> " + codigo + "</html>");
        lblCod.setForeground(txtDialog);
        
        pnlHeader.add(lblProd);
        pnlHeader.add(lblCod);
        dialog.add(pnlHeader, BorderLayout.NORTH);

        DefaultTableModel modeloDetalle = new DefaultTableModel(new String[]{"Sucursal", "Stock Disponible", "Última Actualización"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaDetalle = new JTable(modeloDetalle);
        tablaDetalle.setRowHeight(25);
        dialog.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);

        String sql = "SELECT sucursal, stock_actual, ultima_actualizacion FROM inventario_global WHERE codigo_barras = ?";
        
        try (Connection conn = ConexionNube.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {
             
            pst.setString(1, codigo);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    modeloDetalle.addRow(new Object[]{
                        rs.getString("sucursal"),
                        rs.getInt("stock_actual") + " pz",
                        rs.getString("ultima_actualizacion")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error al cargar detalles: " + e.getMessage());
        }

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());
        JPanel pnlSur = new JPanel();
        pnlSur.setOpaque(false);
        pnlSur.add(btnCerrar);
        dialog.add(pnlSur, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}