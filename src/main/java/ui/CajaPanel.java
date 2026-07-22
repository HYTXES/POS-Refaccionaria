package ui;

import entities.Producto;
import repository.ProductoLocalImpl;
import repository.VentaLocalImpl;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CajaPanel extends JPanel {

    private JTextField txtCodigo;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JLabel lblTotal;
    private JButton btnCobrar, btnCancelar, btnEditarCantidad, btnEliminarArticulo;
    private double totalVenta = 0.0;
    private ProductoLocalImpl repo;
    private VentaLocalImpl repoVentas;

    private JPopupMenu popupBusqueda;
    private JList<String> listaSugerencias;
    private DefaultListModel<String> modeloSugerencias;
    private List<Producto> productosSugeridos;

    public CajaPanel() {
        this.repo = new ProductoLocalImpl();
        this.repoVentas = new VentaLocalImpl();

        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgFondo = isDark ? new Color(15, 23, 42) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");
        Color txtSecundario = isDark ? new Color(148, 163, 184) : Color.GRAY;

        setLayout(new BorderLayout(10, 10));
        setBackground(bgFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pnlSuperior = new JPanel(new BorderLayout(10, 10));
        pnlSuperior.setOpaque(false);

        JLabel lblTitulo = new JLabel("Punto de Venta");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(txtPrincipal); // Color dinámico

        JPanel pnlEscaner = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlEscaner.setOpaque(false);
        
        JLabel lblEscaner = new JLabel("🔍 Buscar o Escanear: ");
        lblEscaner.setForeground(txtPrincipal); // Color dinámico
        pnlEscaner.add(lblEscaner);

        txtCodigo = new JTextField(35);
        txtCodigo.setFont(new Font("SansSerif", Font.PLAIN, 18));
        pnlEscaner.add(txtCodigo);

        JButton btnBuscar = new JButton("Agregar");
        btnBuscar.setFocusable(false); 
        pnlEscaner.add(btnBuscar);

        pnlSuperior.add(lblTitulo, BorderLayout.NORTH);
        pnlSuperior.add(pnlEscaner, BorderLayout.CENTER);

        String[] columnas = {"Cant.", "Código", "Descripción", "Precio U.", "Subtotal"};
        modeloCarrito = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(35);
        tablaCarrito.setFont(new Font("SansSerif", Font.PLAIN, 16));
        tablaCarrito.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tablaCarrito.getTableHeader().setReorderingAllowed(false);
        tablaCarrito.setFocusable(false);

        tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaCarrito.getColumnModel().getColumn(2).setPreferredWidth(300);
        tablaCarrito.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaCarrito.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollTabla = new JScrollPane(tablaCarrito);

        JPanel pnlInferior = new JPanel(new BorderLayout());
        pnlInferior.setOpaque(false);
        pnlInferior.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBotones.setOpaque(false);

        btnEditarCantidad = new JButton("✏️ Modificar Cantidad");
        btnEditarCantidad.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnEditarCantidad.setFocusable(false);

        btnEliminarArticulo = new JButton("❌ Quitar Artículo");
        btnEliminarArticulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnEliminarArticulo.setFocusable(false);

        btnCancelar = new JButton("🚮️ Cancelar Venta");
        btnCancelar.setForeground(isDark ? new Color(255, 100, 100) : new Color(200, 0, 0)); // Rojo más legible en oscuro
        btnCancelar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCancelar.setFocusable(false);

        pnlBotones.add(btnEditarCantidad);
        pnlBotones.add(btnEliminarArticulo);
        pnlBotones.add(btnCancelar);

        JPanel pnlTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        pnlTotal.setOpaque(false);

        lblTotal = new JLabel("Total: $0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTotal.setForeground(isDark ? new Color(100, 255, 100) : new Color(0, 100, 0)); // Verde brillante si es oscuro

        btnCobrar = new JButton("💸 COBRAR");
        btnCobrar.setFont(new Font("SansSerif", Font.BOLD, 24));
        btnCobrar.setBackground(new Color(0, 153, 51));
        btnCobrar.setForeground(Color.WHITE);
        btnCobrar.setFocusPainted(false);
        btnCobrar.setFocusable(false);
        btnCobrar.setPreferredSize(new Dimension(200, 60));

        pnlTotal.add(lblTotal);
        pnlTotal.add(btnCobrar);

        pnlInferior.add(pnlBotones, BorderLayout.WEST);
        pnlInferior.add(pnlTotal, BorderLayout.EAST);

        add(pnlSuperior, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        add(pnlInferior, BorderLayout.SOUTH);

        configurarBuscadorDinamico();
        configurarFocoMagnetico(scrollTabla); 

        txtCodigo.addActionListener(this::buscarYAgregarProductoLocal);
        btnBuscar.addActionListener(this::buscarYAgregarProductoLocal);

        btnEditarCantidad.addActionListener(e -> {
            int fila = tablaCarrito.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un artículo de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String codigo = modeloCarrito.getValueAt(fila, 1).toString();
            int cantActual = (int) modeloCarrito.getValueAt(fila, 0);
            Producto p = repo.buscarPorCodigo(codigo);

            if (p != null) {
                String input = JOptionPane.showInputDialog(this, "¿Nueva cantidad para " + p.getNombre() + "?", String.valueOf(cantActual));
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int nuevaCant = Integer.parseInt(input.trim());
                        if (nuevaCant <= 0) {
                            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if (nuevaCant > p.getStockActual()) {
                            JOptionPane.showMessageDialog(this, "Stock insuficiente. Solo hay " + p.getStockActual() + " disponibles.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        double nuevoSubtotal = nuevaCant * p.getPrecioVenta();
                        modeloCarrito.setValueAt(nuevaCant, fila, 0);
                        modeloCarrito.setValueAt(nuevoSubtotal, fila, 4);
                        calcularTotal();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Ingresa un número entero válido.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnEliminarArticulo.addActionListener(e -> {
            int fila = tablaCarrito.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un artículo de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            modeloCarrito.removeRow(fila);
            calcularTotal();
            txtCodigo.requestFocusInWindow();
        });

        btnCancelar.addActionListener(e -> {
            if (modeloCarrito.getRowCount() > 0) {
                int op = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas cancelar toda la venta?", "Cancelar Venta", JOptionPane.YES_NO_OPTION);
                if (op == JOptionPane.YES_OPTION) {
                    limpiarCarrito();
                }
            }
        });

        btnCobrar.addActionListener(e -> {
            if (!new repository.CajaLocalImpl().hayCajaAbierta()) {
                JOptionPane.showMessageDialog(this, "Operación Denegada: La caja registradora está CERRADA.\nDebes iniciar un turno para poder procesar ventas.", "Caja Cerrada", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (modeloCarrito.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, String.format("¿Confirmar cobro por $%.2f?", totalVenta), "Confirmar Venta", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String vendedor = (entities.Sesion.getUsuarioActual() != null)
                        ? entities.Sesion.getUsuarioActual().getUsername()
                        : "Usuario de Prueba";

                String idVenta = repoVentas.procesarVenta(modeloCarrito, totalVenta, vendedor);

                if (idVenta != null) {
                    JOptionPane.showMessageDialog(this, "Venta registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    util.TicketGenerator.generarTicket(idVenta, modeloCarrito, totalVenta, vendedor);
                    limpiarCarrito();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al registrar la venta.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        SwingUtilities.invokeLater(() -> txtCodigo.requestFocusInWindow());
    }

    private void configurarFocoMagnetico(JScrollPane scrollTabla) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (this.isShowing() && e.getID() == KeyEvent.KEY_TYPED) {
                Window ventanaActiva = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();

                if (ventanaActiva != null && ventanaActiva == SwingUtilities.getWindowAncestor(this)) {
                    if (!txtCodigo.isFocusOwner()) {
                        txtCodigo.requestFocusInWindow(); 
                        txtCodigo.setText(txtCodigo.getText() + e.getKeyChar()); 
                        return true; 
                    }
                }
            }
            return false;
        });

        MouseAdapter recuperadorDeFoco = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                txtCodigo.requestFocusInWindow();
            }
        };
        this.addMouseListener(recuperadorDeFoco);
        tablaCarrito.addMouseListener(recuperadorDeFoco);
        scrollTabla.getViewport().addMouseListener(recuperadorDeFoco);
    }

    private void configurarBuscadorDinamico() {
        popupBusqueda = new JPopupMenu();
        popupBusqueda.setFocusable(false);
        modeloSugerencias = new DefaultListModel<>();
        listaSugerencias = new JList<>(modeloSugerencias);
        listaSugerencias.setFont(new Font("SansSerif", Font.PLAIN, 16));
        productosSugeridos = new ArrayList<>();

        JScrollPane scrollSugerencias = new JScrollPane(listaSugerencias);
        scrollSugerencias.setPreferredSize(new Dimension(500, 200));
        popupBusqueda.add(scrollSugerencias);

        txtCodigo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { actualizarSugerencias(); }
            @Override
            public void removeUpdate(DocumentEvent e) { actualizarSugerencias(); }
            @Override
            public void changedUpdate(DocumentEvent e) { actualizarSugerencias(); }
        });

        txtCodigo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && popupBusqueda.isVisible()) {
                    listaSugerencias.requestFocus();
                    listaSugerencias.setSelectedIndex(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupBusqueda.setVisible(false);
                }
            }
        });

        listaSugerencias.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { seleccionarDeSugerencias(); }
        });

        listaSugerencias.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    seleccionarDeSugerencias();
                } else if (e.getKeyCode() == KeyEvent.VK_UP && listaSugerencias.getSelectedIndex() == 0) {
                    txtCodigo.requestFocus();
                    txtCodigo.setCaretPosition(txtCodigo.getText().length());
                }
            }
        });
    }

    private void actualizarSugerencias() {
        SwingUtilities.invokeLater(() -> {
            String texto = txtCodigo.getText().trim();
            if (texto.isEmpty()) {
                popupBusqueda.setVisible(false);
                return;
            }

            List<Producto> resultados = repo.buscarPorFiltro(texto);
            if (resultados.isEmpty()) {
                popupBusqueda.setVisible(false);
                return;
            }

            modeloSugerencias.clear();
            productosSugeridos.clear();
            for (Producto p : resultados) {
                modeloSugerencias.addElement(p.getCodigoBarras() + " | " + p.getNombre() + " (" + p.getMarca() + ") | $" + p.getPrecioVenta());
                productosSugeridos.add(p);
            }

            if (!popupBusqueda.isVisible() && txtCodigo.isFocusOwner()) {
                popupBusqueda.show(txtCodigo, 0, txtCodigo.getHeight());
            }
        });
    }

    private void seleccionarDeSugerencias() {
        int index = listaSugerencias.getSelectedIndex();
        if (index != -1) {
            Producto p = productosSugeridos.get(index);
            popupBusqueda.setVisible(false);
            procesarProducto(p);
        }
    }

    private void buscarYAgregarProductoLocal(ActionEvent e) {
        String texto = txtCodigo.getText().trim();
        if (texto.isEmpty()) { return; }

        popupBusqueda.setVisible(false);
        Producto p = repo.buscarPorCodigo(texto);

        if (p == null) {
            if (productosSugeridos.size() == 1) {
                p = productosSugeridos.get(0);
            } else {
                JOptionPane.showMessageDialog(this, "Refacción no encontrada.", "Error", JOptionPane.WARNING_MESSAGE);
                txtCodigo.selectAll();
                return;
            }
        }
        procesarProducto(p);
    }

    private void procesarProducto(Producto p) {
        if (p.getStockActual() <= 0) {
            JOptionPane.showMessageDialog(this, "No hay stock disponible de: " + p.getNombre(), "Sin Stock", JOptionPane.ERROR_MESSAGE);
            txtCodigo.setText("");
            txtCodigo.requestFocusInWindow();
            return;
        }

        agregarAlCarrito(p);
        txtCodigo.setText("");
        txtCodigo.requestFocusInWindow();
    }

    private void agregarAlCarrito(Producto p) {
        boolean existe = false;

        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            String codigoFila = modeloCarrito.getValueAt(i, 1).toString();
            if (codigoFila.equals(p.getCodigoBarras())) {
                int cantActual = (int) modeloCarrito.getValueAt(i, 0);

                if (cantActual >= p.getStockActual()) {
                    JOptionPane.showMessageDialog(this, "Stock insuficiente. Solo hay " + p.getStockActual() + " disponibles.");
                    return;
                }

                int nuevaCant = cantActual + 1;
                double nuevoSubtotal = nuevaCant * p.getPrecioVenta();

                modeloCarrito.setValueAt(nuevaCant, i, 0);
                modeloCarrito.setValueAt(nuevoSubtotal, i, 4);
                existe = true;
                break;
            }
        }

        if (!existe) {
            modeloCarrito.addRow(new Object[]{
                1,
                p.getCodigoBarras(),
                p.getNombre() + " (" + p.getMarca() + ")",
                p.getPrecioVenta(),
                p.getPrecioVenta()
            });
        }

        calcularTotal();
    }

    private void calcularTotal() {
        totalVenta = 0.0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            totalVenta += (double) modeloCarrito.getValueAt(i, 4);
        }
        lblTotal.setText(String.format("Total: $%.2f", totalVenta));
    }

    private void limpiarCarrito() {
        modeloCarrito.setRowCount(0);
        calcularTotal();
        txtCodigo.setText("");
        txtCodigo.requestFocusInWindow();
    }
}