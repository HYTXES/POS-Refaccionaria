package ui;

import entities.Producto;
import entities.Proveedor;
import repository.ProveedorLocalImpl;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FormProducto extends JDialog {

    private JTextField txtCodigo, txtNombre, txtMarca, txtPrecioCompra, txtPrecioVenta, txtStock, txtStockCritico;
    private JComboBox<String> comboProveedor;
    private JTextArea txtDescripcion;
    private JButton btnGuardar, btnCancelar;
    private boolean guardadoExitoso = false;
    private String idOriginal = null;

    public FormProducto(Frame parent) {
        super(parent, "Registro de Refacción", true);
        setSize(500, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgTarjetas = isDark ? new Color(30, 41, 59) : Color.WHITE;
        Color txtPrincipal = isDark ? Color.WHITE : Color.decode("#1E293B");

        getContentPane().setBackground(bgTarjetas);

        JPanel pnlCampos = new JPanel(new GridBagLayout());
        pnlCampos.setOpaque(false);
        pnlCampos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labelsText = {
            "Código de Barras:", "Nombre:", "Marca:", "Proveedor:", 
            "P. Compra ($):", "P. Venta ($):", "Stock Inicial:", 
            "Stock Mínimo:", "Descripción:"
        };

        Component[] inputs = {
            txtCodigo = new JTextField(15),
            txtNombre = new JTextField(15),
            txtMarca = new JTextField(15),
            comboProveedor = new JComboBox<>(),
            txtPrecioCompra = new JTextField(7),
            txtPrecioVenta = new JTextField(7),
            txtStock = new JTextField(5),
            txtStockCritico = new JTextField(5),
            null
        };

        cargarProveedores();

        for (int i = 0; i < labelsText.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel lbl = new JLabel(labelsText[i]);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl.setForeground(txtPrincipal);
            pnlCampos.add(lbl, gbc);

            gbc.gridx = 1;
            if (i == 8) {
                txtDescripcion = new JTextArea(3, 15);
                txtDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 14));
                txtDescripcion.setLineWrap(true);
                JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
                pnlCampos.add(scrollDesc, gbc);
            } else {
                JComponent comp = (JComponent) inputs[i];
                comp.setFont(new Font("SansSerif", Font.PLAIN, 14));
                pnlCampos.add(comp, gbc);
            }
        }

        add(pnlCampos, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotones.setOpaque(false);
        pnlBotones.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFocusable(false);

        btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(Color.decode("#0F43AB"));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setFocusable(false);

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);
        add(pnlBotones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> {
            if (validarDatos()) {
                guardadoExitoso = true;
                dispose();
            }
        });
    }

    private void cargarProveedores() {
        ProveedorLocalImpl repoProv = new ProveedorLocalImpl();
        List<Proveedor> lista = repoProv.obtenerTodos();
        comboProveedor.addItem("Seleccionar...");
        for (Proveedor p : lista) {
            comboProveedor.addItem(p.getNombre());
        }
    }

    public void cargarProducto(Producto p) {
        this.idOriginal = p.getId();
        txtCodigo.setText(p.getCodigoBarras());
        txtCodigo.setEditable(false);
        txtNombre.setText(p.getNombre());
        txtMarca.setText(p.getMarca());
        if (p.getNumeroParte() != null) {
            comboProveedor.setSelectedItem(p.getNumeroParte());
        }
        txtPrecioCompra.setText(String.valueOf(p.getPrecioCompra()));
        txtPrecioVenta.setText(String.valueOf(p.getPrecioVenta()));
        txtStock.setText(String.valueOf(p.getStockActual()));
        txtStockCritico.setText(String.valueOf(p.getStockCritico()));
        txtDescripcion.setText(p.getVehiculosCompatibles());
    }

    private boolean validarDatos() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty() || comboProveedor.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Código, Nombre y Proveedor son obligatorios", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Double.parseDouble(txtPrecioCompra.getText().trim());
            Double.parseDouble(txtPrecioVenta.getText().trim());
            Integer.parseInt(txtStock.getText().trim());
            Integer.parseInt(txtStockCritico.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Revisa los valores numéricos", "Error de formato", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public Producto getProducto() {
        if (!guardadoExitoso) return null;
        Producto p = new Producto();
        if (idOriginal != null) {
            p.setId(idOriginal);
        }
        p.setCodigoBarras(txtCodigo.getText().trim());
        p.setNombre(txtNombre.getText().trim());
        p.setMarca(txtMarca.getText().trim());
        p.setVehiculosCompatibles(txtDescripcion.getText().trim());
        p.setPrecioCompra(Double.parseDouble(txtPrecioCompra.getText().trim()));
        p.setPrecioVenta(Double.parseDouble(txtPrecioVenta.getText().trim()));
        p.setStockActual(Integer.parseInt(txtStock.getText().trim()));
        p.setStockCritico(Integer.parseInt(txtStockCritico.getText().trim()));
        p.setNumeroParte(comboProveedor.getSelectedItem().toString());
        return p;
    }
}