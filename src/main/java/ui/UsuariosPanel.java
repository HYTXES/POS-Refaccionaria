package ui;

import entities.Sesion;
import entities.Usuario;
import repository.UsuarioLocalImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsuariosPanel extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private UsuarioLocalImpl repo;
    private List<Usuario> listaUsuarios;

    public UsuariosPanel() {
        this.repo = new UsuarioLocalImpl();
        
        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgFondo = isDark ? new Color(15, 23, 42) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");

        setLayout(new BorderLayout(10, 10));
        setBackground(bgFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("Gestión de Usuarios del Sistema");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(txtPrincipal); // Color dinámico
        add(lblTitulo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"ID", "Usuario", "Rol de Acceso"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(30);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 15));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- PANEL DE ACCIONES CON BOTONES POR DEFECTO ---
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAcciones.setOpaque(false);
        
        JButton btnNuevo = new JButton("➕ Agregar Usuario");
        JButton btnPassword = new JButton("🔑 Cambiar Contraseña");
        JButton btnEliminar = new JButton("🚮 Eliminar Usuario");
        btnEliminar.setForeground(isDark ? new Color(255, 100, 100) : new Color(200, 0, 0)); // Rojo brillante en oscuro

        pnlAcciones.add(btnNuevo);
        pnlAcciones.add(btnPassword);
        pnlAcciones.add(btnEliminar);
        add(pnlAcciones, BorderLayout.SOUTH);

        // --- EVENTOS ---

        // 1. Agregar Usuario
        btnNuevo.addActionListener(e -> {
            JTextField fUser = new JTextField();
            JPasswordField fPass = new JPasswordField();
            JComboBox<String> comboRol = new JComboBox<>(new String[]{"Administrador", "Cajero"});
            
            Object[] msg = {
                "Nombre de Usuario:", fUser, 
                "Contraseña:", fPass, 
                "Rol:", comboRol
            };

            int op = JOptionPane.showConfirmDialog(this, msg, "Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op == JOptionPane.OK_OPTION) {
                String user = fUser.getText().trim();
                String pass = new String(fPass.getPassword()).trim();
                
                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El usuario y contraseña son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Usuario u = new Usuario();
                u.setUsername(user);
                u.setPassword(pass);
                u.setRol(comboRol.getSelectedItem().toString());
                
                if (repo.guardar(u)) {
                    cargarDatos();
                    JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar. Tal vez el nombre de usuario ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 2. Cambiar Contraseña
        btnPassword.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Usuario u = listaUsuarios.get(fila);
            JPasswordField fPass = new JPasswordField();
            Object[] msg = {"Nueva contraseña para " + u.getUsername() + ":", fPass};

            int op = JOptionPane.showConfirmDialog(this, msg, "Cambiar Contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op == JOptionPane.OK_OPTION) {
                String newPass = new String(fPass.getPassword()).trim();
                if (!newPass.isEmpty()) {
                    if (repo.actualizarPassword(u.getId(), newPass)) {
                        JOptionPane.showMessageDialog(this, "Contraseña actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al actualizar la contraseña.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // 3. Eliminar Usuario
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Usuario u = listaUsuarios.get(fila);

            // No permitir eliminar la sesión activa actual
            if (Sesion.getUsuarioActual() != null && u.getUsername().equals(Sesion.getUsuarioActual().getUsername())) {
                JOptionPane.showMessageDialog(this, 
                    "No puedes eliminar tu propio usuario mientras tienes la sesión activa.", 
                    "Acción Denegada", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Garantizar que siempre exista al menos un Administrador
            if (u.getRol().equals("Administrador")) {
                long totalAdmins = listaUsuarios.stream()
                                                    .filter(usr -> usr.getRol().equals("Administrador"))
                                                    .count();
                if (totalAdmins <= 1) {
                    JOptionPane.showMessageDialog(this, 
                        "No puedes eliminar a este usuario.\nDebe existir al menos un Administrador en el sistema.", 
                        "Acción Denegada", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Estás completamente seguro de eliminar al usuario: " + u.getUsername() + "?\nEsta acción no se puede deshacer.", 
                "Confirmación de Seguridad", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (repo.eliminar(u.getId())) {
                    cargarDatos();
                    JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Cargar datos al mostrar el panel
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
        listaUsuarios = repo.obtenerTodos();

        for (Usuario u : listaUsuarios) {
            String idMostrar = (u.getId() != null && u.getId().length() >= 8) ? u.getId().substring(0, 8) : u.getId();
            modelo.addRow(new Object[]{idMostrar, u.getUsername(), u.getRol()});
        }
    }
}