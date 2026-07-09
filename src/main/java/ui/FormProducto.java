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

        JPanel pnlCampos = new JPanel(new GridBagLayout());
        pnlCampos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        pnlCampos.add(new JLabel("Código de Barras:"), gbc);
        txtCodigo = new JTextField(15);
        gbc.gridx = 1; pnlCampos.add(txtCodigo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        pnlCampos.add(new JLabel("Nombre:"), gbc);
        txtNombre = new JTextField(15);
        gbc.gridx = 1; pnlCampos.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        pnlCampos.add(new JLabel("Marca:"), gbc);
        txtMarca = new JTextField(15);
        gbc.gridx = 1; pnlCampos.add(txtMarca, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        pnlCampos.add(new JLabel("Proveedor:"), gbc);
        comboProveedor = new JComboBox<>();
        cargarProveedores();
        gbc.gridx = 1; pnlCampos.add(comboProveedor, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        pnlCampos.add(new JLabel("P. Compra ($):"), gbc);
        txtPrecioCompra = new JTextField(7);
        gbc.gridx = 1; pnlCampos.add(txtPrecioCompra, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        pnlCampos.add(new JLabel("P. Venta ($):"), gbc);
        txtPrecioVenta = new JTextField(7);
        gbc.gridx = 1; pnlCampos.add(txtPrecioVenta, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        pnlCampos.add(new JLabel("Stock Inicial:"), gbc);
        txtStock = new JTextField(5);
        gbc.gridx = 1; pnlCampos.add(txtStock, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        pnlCampos.add(new JLabel("Stock Mínimo:"), gbc);
        txtStockCritico = new JTextField(5);
        gbc.gridx = 1; pnlCampos.add(txtStockCritico, gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        pnlCampos.add(new JLabel("Descripción:"), gbc);
        txtDescripcion = new JTextArea(3, 15);
        txtDescripcion.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        gbc.gridx = 1; pnlCampos.add(scrollDesc, gbc);

        add(pnlCampos, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
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
            JOptionPane.showMessageDialog(this, "Código, Nombre y Proveedor son obligatorios");
            return false;
        }
        try {
            Double.parseDouble(txtPrecioCompra.getText());
            Double.parseDouble(txtPrecioVenta.getText());
            Integer.parseInt(txtStock.getText());
            Integer.parseInt(txtStockCritico.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Revisa los valores numéricos");
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
        p.setCodigoBarras(txtCodigo.getText());
        p.setNombre(txtNombre.getText());
        p.setMarca(txtMarca.getText());
        p.setVehiculosCompatibles(txtDescripcion.getText());
        p.setPrecioCompra(Double.parseDouble(txtPrecioCompra.getText()));
        p.setPrecioVenta(Double.parseDouble(txtPrecioVenta.getText()));
        p.setStockActual(Integer.parseInt(txtStock.getText()));
        p.setStockCritico(Integer.parseInt(txtStockCritico.getText()));
        p.setNumeroParte(comboProveedor.getSelectedItem().toString());
        return p;
    }
}