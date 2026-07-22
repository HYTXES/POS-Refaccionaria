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

public class ConsultaProductoPanel extends JPanel {

    private JTable tablaProductos;
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscar;
    private JComboBox<Object> comboProveedores;
    private ProductoLocalImpl repoProd;
    private ProveedorLocalImpl repoProv;

    public ConsultaProductoPanel() {
        this.repoProd = new ProductoLocalImpl();
        this.repoProv = new ProveedorLocalImpl();

        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgFondo = isDark ? new Color(15, 23, 42) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");

        setLayout(new BorderLayout(10, 10));
        setBackground(bgFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pnlHerramientas = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlHerramientas.setOpaque(false);

        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBusqueda.setOpaque(false);
        
        JLabel lblBuscar = new JLabel("🔍 Buscar: ");
        lblBuscar.setForeground(txtPrincipal);
        pnlBusqueda.add(lblBuscar);
        
        txtBuscar = new JTextField(25);
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 16));
        pnlBusqueda.add(txtBuscar);

        JLabel lblFiltro = new JLabel("  🏢 Filtrar Proveedor: ");
        lblFiltro.setForeground(txtPrincipal);
        pnlBusqueda.add(lblFiltro);
        
        comboProveedores = new JComboBox<>();
        comboProveedores.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pnlBusqueda.add(comboProveedores);

        JLabel lblTitulo = new JLabel("Catálogo de Productos", JLabel.LEFT);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(txtPrincipal);
        
        pnlHerramientas.add(lblTitulo);
        pnlHerramientas.add(pnlBusqueda);

        String[] columnas = {"Código", "Nombre", "Marca", "Proveedor", "Descripción", "P. Venta", "Stock"};
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

        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(160);
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(120);
        tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(200);
        tablaProductos.getColumnModel().getColumn(5).setPreferredWidth(80);
        tablaProductos.getColumnModel().getColumn(6).setPreferredWidth(60);

        // --- RENDERIZADOR ADAPTABLE AL TEMA (STOCK AGOTADO) ---
        tablaProductos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    try {
                        int stockActual = Integer.parseInt(table.getValueAt(row, 6).toString());
                        if (stockActual <= 0) {
                            // Alerta de stock adaptada para claro u oscuro
                            c.setBackground(isDark ? new Color(60, 20, 20) : new Color(255, 230, 230));
                            c.setForeground(isDark ? new Color(255, 120, 120) : new Color(180, 0, 0));
                        } else {
                            // Colores por defecto de la tabla según FlatLaf
                            c.setBackground(table.getBackground());
                            c.setForeground(table.getForeground());
                        }
                    } catch (Exception ignored) {}
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
                sorter.setRowFilter(RowFilter.regexFilter(selected.toString(), 3));
            }
        });

        add(pnlHerramientas, BorderLayout.NORTH);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                cargarDatos();
                actualizarCombos();
            }
        });

        actualizarCombos();
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
                p.getCodigoBarras(),
                p.getNombre(),
                p.getMarca(),
                p.getNumeroParte(),
                p.getVehiculosCompatibles(),
                "$" + String.format("%.2f", p.getPrecioVenta()),
                p.getStockActual()
            });
        }
    }
}