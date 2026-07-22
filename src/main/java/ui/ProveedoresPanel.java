package ui;

import entities.Proveedor;
import repository.ProveedorLocalImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProveedoresPanel extends JPanel {
    
    private JTable tabla;
    private DefaultTableModel modelo;
    private ProveedorLocalImpl repo;
    private List<Proveedor> listaProveedores;

    public ProveedoresPanel() {
        this.repo = new ProveedorLocalImpl();
        
        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgFondo = isDark ? new Color(15, 23, 42) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");

        setLayout(new BorderLayout(10, 10));
        setBackground(bgFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("Directorio de Proveedores");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(txtPrincipal); // Color dinámico
        add(lblTitulo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Contacto", "Teléfono", "Correo"}, 0) {
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

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAcciones.setOpaque(false);
        
        JButton btnNuevo = new JButton("➕ Nuevo Proveedor");
        JButton btnEditar = new JButton("📝 Modificar");
        JButton btnEliminar = new JButton("🚮 Dar de baja");
        btnEliminar.setForeground(isDark ? new Color(255, 100, 100) : new Color(200, 0, 0)); // Rojo brillante en oscuro

        pnlAcciones.add(btnNuevo);
        pnlAcciones.add(btnEditar);
        pnlAcciones.add(btnEliminar);
        add(pnlAcciones, BorderLayout.SOUTH);

        btnNuevo.addActionListener(e -> {
            JTextField fNombre = new JTextField();
            JTextField fContacto = new JTextField();
            JTextField fTel = new JTextField();
            JTextField fCorreo = new JTextField();
            Object[] msg = {"Nombre:", fNombre, "Contacto:", fContacto, "Teléfono:", fTel, "Correo:", fCorreo};

            int op = JOptionPane.showConfirmDialog(this, msg, "Registro de Proveedor", JOptionPane.OK_CANCEL_OPTION);
            if (op == JOptionPane.OK_OPTION) {
                Proveedor p = new Proveedor();
                p.setNombre(fNombre.getText());
                p.setContacto(fContacto.getText());
                p.setTelefono(fTel.getText());
                p.setCorreo(fCorreo.getText());
                if (repo.guardar(p)) {
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un proveedor para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Proveedor p = listaProveedores.get(fila);

            JTextField fNombre = new JTextField(p.getNombre());
            JTextField fContacto = new JTextField(p.getContacto());
            JTextField fTel = new JTextField(p.getTelefono());
            JTextField fCorreo = new JTextField(p.getCorreo());
            Object[] msg = {"Nombre:", fNombre, "Contacto:", fContacto, "Teléfono:", fTel, "Correo:", fCorreo};

            int op = JOptionPane.showConfirmDialog(this, msg, "Modificar Proveedor", JOptionPane.OK_CANCEL_OPTION);
            if (op == JOptionPane.OK_OPTION) {
                p.setNombre(fNombre.getText());
                p.setContacto(fContacto.getText());
                p.setTelefono(fTel.getText());
                p.setCorreo(fCorreo.getText());
                
                if (repo.actualizar(p)) {
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un proveedor para dar de baja.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Proveedor p = listaProveedores.get(fila);

            int confirm = JOptionPane.showConfirmDialog(this, "¿Dar de baja al proveedor: " + p.getNombre() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (repo.eliminar(p.getId())) {
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                cargarDatos();
            }
        });

        cargarDatos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        listaProveedores = repo.obtenerTodos();

        for (Proveedor p : listaProveedores) {
            String idMostrar = (p.getId() != null && p.getId().length() >= 8) ? p.getId().substring(0, 8) : p.getId();
            modelo.addRow(new Object[]{idMostrar, p.getNombre(), p.getContacto(), p.getTelefono(), p.getCorreo()});
        }
    }
}