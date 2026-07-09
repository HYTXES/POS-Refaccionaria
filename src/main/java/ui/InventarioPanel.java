package ui;

import entities.Producto;
import entities.Proveedor;
import repository.ProductoLocalImpl;
import repository.ProveedorLocalImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class InventarioPanel extends JPanel {

    private JTable tablaProductos;
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscar;
    private JComboBox<Object> comboProveedores;
    private ProductoLocalImpl repoProd;
    private ProveedorLocalImpl repoProv;

    public InventarioPanel() {
        this.repoProd = new ProductoLocalImpl();
        this.repoProv = new ProveedorLocalImpl();

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pnlHerramientas = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlHerramientas.setOpaque(false);

        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBusqueda.setOpaque(false);
        pnlBusqueda.add(new JLabel("Buscar: "));
        txtBuscar = new JTextField(20);
        pnlBusqueda.add(txtBuscar);

        pnlBusqueda.add(new JLabel(" Filtrar Proveedor: "));
        comboProveedores = new JComboBox<>();
        pnlBusqueda.add(comboProveedores);

        pnlHerramientas.add(new JLabel("Gestión de Inventario", JLabel.LEFT) {
            {
                setFont(new Font("Arial", Font.BOLD, 22));
            }
        });
        pnlHerramientas.add(pnlBusqueda);

        String[] columnas = {"ID", "Código", "Nombre", "Marca", "Proveedor", "Descripción", "P. Compra", "P. Venta", "Stock", "Mínimo"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modelo);
        sorter = new TableRowSorter<>(modelo);
        tablaProductos.setRowSorter(sorter);
        tablaProductos.setRowHeight(30);
        tablaProductos.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tablaProductos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tablaProductos.getTableHeader().setReorderingAllowed(false);

        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(30);
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(90);
        tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(5).setPreferredWidth(180);
        tablaProductos.getColumnModel().getColumn(8).setPreferredWidth(45);
        tablaProductos.getColumnModel().getColumn(9).setPreferredWidth(50);

        tablaProductos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                int stockActual = Integer.parseInt(table.getValueAt(row, 8).toString());
                int stockCritico = Integer.parseInt(table.getValueAt(row, 9).toString());

                if (!isSelected) {
                    if (stockActual <= stockCritico) {
                        c.setBackground(new Color(255, 230, 230));
                        c.setForeground(new Color(180, 0, 0));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtBuscar.getText()));
            }
        });

        comboProveedores.addActionListener(e -> {
            Object selected = comboProveedores.getSelectedItem();
            if (selected == null || selected.toString().equals("Todos")) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter(selected.toString(), 4));
            }
        });

        add(pnlHerramientas, BorderLayout.NORTH);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAcciones.setOpaque(false);

        JButton btnNuevo = new JButton("➕ Nuevo producto");
        JButton btnReabastecer = new JButton("📦 Reabastecer Stock");
        JButton btnEditar = new JButton("📝 Modificar");
        JButton btnEliminar = new JButton("🚮 Dar de baja");
        btnEliminar.setForeground(new Color(200, 0, 0));

        pnlAcciones.add(btnNuevo);
        pnlAcciones.add(btnReabastecer);
        pnlAcciones.add(btnEditar);
        pnlAcciones.add(btnEliminar);

        btnNuevo.addActionListener(e -> {
            Window parentWindow = SwingUtilities.windowForComponent(this);
            FormProducto form = new FormProducto((Frame) parentWindow);
            form.setVisible(true);

            Producto nuevo = form.getProducto();
            if (nuevo != null) {
                if (repoProd.guardar(nuevo)) {
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnReabastecer.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto para reabastecer", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String codigo = tablaProductos.getValueAt(fila, 1).toString();
            String nombre = tablaProductos.getValueAt(fila, 2).toString();
            
            String input = JOptionPane.showInputDialog(this, "¿Cuántas piezas de '" + nombre + "' ingresan?", "Reabastecer Stock", JOptionPane.QUESTION_MESSAGE);
            
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int cantidadLlegada = Integer.parseInt(input.trim());
                    if (cantidadLlegada <= 0) {
                        JOptionPane.showMessageDialog(this, "Debes ingresar una cantidad mayor a cero", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    Producto p = repoProd.buscarPorCodigo(codigo);
                    if (p != null) {
                        p.setStockActual(p.getStockActual() + cantidadLlegada);
                        if (repoProd.actualizar(p)) {
                            cargarDatos();
                            JOptionPane.showMessageDialog(this, "Stock reabastecido correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Error al actualizar la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Ingresa un número válido", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEditar.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto para modificar", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String codigo = tablaProductos.getValueAt(fila, 1).toString();
            Producto p = repoProd.buscarPorCodigo(codigo);
            if (p != null) {
                Window parentWindow = SwingUtilities.windowForComponent(this);
                FormProducto form = new FormProducto((Frame) parentWindow);
                form.cargarProducto(p);
                form.setVisible(true);

                Producto editado = form.getProducto();
                if (editado != null) {
                    if (repoProd.actualizar(editado)) {
                        cargarDatos();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al actualizar", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto para dar de baja", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String codigo = tablaProductos.getValueAt(fila, 1).toString();
            String nombre = tablaProductos.getValueAt(fila, 2).toString();
            
            int confirm = JOptionPane.showConfirmDialog(this, "¿Dar de baja el producto: " + nombre + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (repoProd.eliminar(codigo)) {
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(pnlAcciones, BorderLayout.SOUTH);

        actualizarCombos();

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                cargarDatos();
            }
        });

        cargarDatos();
    }

    private void actualizarCombos() {
        comboProveedores.removeAllItems();
        comboProveedores.addItem("Todos");
        List<Proveedor> provs = repoProv.obtenerTodos();
        for (Proveedor p : provs) {
            comboProveedores.addItem(p.getNombre());
        }
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        List<Producto> lista = repoProd.obtenerTodos();

        for (Producto p : lista) {
            modelo.addRow(new Object[]{
                p.getId().substring(0, 5),
                p.getCodigoBarras(),
                p.getNombre(),
                p.getMarca(),
                p.getNumeroParte(),
                p.getVehiculosCompatibles(),
                "$" + p.getPrecioCompra(),
                "$" + p.getPrecioVenta(),
                p.getStockActual(),
                p.getStockCritico()
            });
        }
    }
}