package ui;

import repository.CajaLocalImpl;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class CortesCajaPanel extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private CajaLocalImpl repoCaja;
    private TableRowSorter<DefaultTableModel> sorter;

    public CortesCajaPanel() {
        this.repoCaja = new CajaLocalImpl();
        
        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgFondo = isDark ? new Color(15, 23, 42) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");

        setLayout(new BorderLayout(10, 10));
        setBackground(bgFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- ZONA SUPERIOR: Título y Buscador ---
        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.setOpaque(false);

        JLabel lblTitulo = new JLabel("Historial de Cortes de Caja");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(txtPrincipal);
        pnlNorte.add(lblTitulo, BorderLayout.WEST);

        // Barra de Búsqueda Dinámica
        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBusqueda.setOpaque(false);
        
        JLabel lblBuscar = new JLabel("🔍 Buscar: ");
        lblBuscar.setForeground(txtPrincipal);
        pnlBusqueda.add(lblBuscar);
        
        JTextField txtBuscar = new JTextField(20);
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pnlBusqueda.add(txtBuscar);
        pnlNorte.add(pnlBusqueda, BorderLayout.EAST);

        add(pnlNorte, BorderLayout.NORTH);

        // --- TABLA DE DATOS ---
        String[] columnas = {"ID Oculto", "Fecha Cierre", "Cajero", "Fondo Inicial", "Ventas Turno", "Total Físico", "Diferencia", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(30);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setFocusable(false);

        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(0).setWidth(0);

        tabla.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(250);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- ZONA INFERIOR: Acciones ---
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSur.setOpaque(false);

        JButton btnEliminar = new JButton("🚮️ Eliminar Registro");
        btnEliminar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnEliminar.setForeground(isDark ? new Color(255, 100, 100) : new Color(220, 38, 38));
        btnEliminar.setFocusable(false);
        pnlSur.add(btnEliminar);

        add(pnlSur, BorderLayout.SOUTH);

        // --- EVENTOS ---
        
        // 1. Filtrado en tiempo real al escribir
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
            
            private void filtrar() {
                String texto = txtBuscar.getText();
                if (texto.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
                }
            }
        });

        // 2. Acción de Eliminar
        btnEliminar.addActionListener(e -> {
            int filaSeleccionada = tabla.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un registro de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int filaModelo = tabla.convertRowIndexToModel(filaSeleccionada);
            
            String idRegistro = modelo.getValueAt(filaModelo, 0).toString();
            String fechaRegistro = modelo.getValueAt(filaModelo, 1).toString();

            int confirm = JOptionPane.showConfirmDialog(this, 
                "⚠️ ¿Eliminar permanentemente el corte del " + fechaRegistro + "?\nEsta acción no se puede deshacer.", 
                "Confirmación de Seguridad", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (repoCaja.eliminarCorte(idRegistro)) {
                    JOptionPane.showMessageDialog(this, "Registro eliminado con éxito.");
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar el registro.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 3. Carga automática al abrir la pestaña
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                cargarDatos();
                txtBuscar.setText(""); 
            }
        });

        cargarDatos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        List<String[]> historial = repoCaja.obtenerHistorialCortes();
        for (String[] fila : historial) {
            modelo.addRow(fila);
        }
    }
}