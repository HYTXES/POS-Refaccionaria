package ui;

import entities.Venta;
import repository.VentaLocalImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class HistorialVentasPanel extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private VentaLocalImpl repo;
    private List<Venta> listaVentas;
    private JTextField txtBuscar;

    public HistorialVentasPanel() {
        this.repo = new VentaLocalImpl();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(java.awt.Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pnlHerramientas = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlHerramientas.setOpaque(false);

        JLabel lblTitulo = new JLabel("Historial de Ventas (Corte de Caja)");
        lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFiltros.setOpaque(false);
        
        pnlFiltros.add(new JLabel("🔍 Filtrar: "));
        txtBuscar = new JTextField(25);
        txtBuscar.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16));
        pnlFiltros.add(txtBuscar);

        JButton btnExportarExcel = new JButton("📊 Generar Reporte Detallado");
        btnExportarExcel.setBackground(new java.awt.Color(33, 115, 70));
        btnExportarExcel.setForeground(java.awt.Color.WHITE);
        btnExportarExcel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        btnExportarExcel.setFocusPainted(false);
        pnlFiltros.add(Box.createHorizontalStrut(20));
        pnlFiltros.add(btnExportarExcel);

        pnlHerramientas.add(lblTitulo);
        pnlHerramientas.add(pnlFiltros);
        add(pnlHerramientas, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"Folio Ticket", "Fecha y Hora", "Vendedor", "Total de Venta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo);
        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        tabla.setRowHeight(30);
        tabla.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16));
        tabla.getTableHeader().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAcciones.setOpaque(false);
        JButton btnEliminar = new JButton("🚮️ Eliminar Registro");
        btnEliminar.setForeground(new java.awt.Color(200, 0, 0));
        pnlAcciones.add(btnEliminar);
        add(pnlAcciones, BorderLayout.SOUTH);

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtBuscar.getText()));
            }
        });

        btnExportarExcel.addActionListener(e -> exportarAExcel());

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) return;
            int filaModelo = tabla.convertRowIndexToModel(fila);
            String folio = modelo.getValueAt(filaModelo, 0).toString();
            Venta vEliminar = listaVentas.stream().filter(v -> v.getId().toUpperCase().startsWith(folio)).findFirst().orElse(null);
            if (vEliminar == null) return;
            int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar folio " + folio + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (repo.eliminarVenta(vEliminar.getId())) cargarDatos();
            }
        });

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) { cargarDatos(); }
        });

        cargarDatos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        listaVentas = repo.obtenerHistorial();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        for (Venta v : listaVentas) {
            String folio = v.getId().substring(0, 8).toUpperCase();
            String fecha = v.getFechaVenta() != null ? sdf.format(v.getFechaVenta()) : "N/A";
            modelo.addRow(new Object[]{folio, fecha, v.getVendedor(), "$" + v.getTotal()});
        }
    }

    private void exportarAExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = fileChooser.getSelectedFile();
        if (!archivo.getName().endsWith(".xlsx")) archivo = new File(archivo.getAbsolutePath() + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            // --- ESTILOS ---
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // --- HOJA 1: RESUMEN DE VENTAS ---
            Sheet sheet1 = workbook.createSheet("Resumen de Ventas");
            Row rowHeader1 = sheet1.createRow(0);
            String[] col1 = {"Folio", "Fecha", "Vendedor", "Total"};
            for (int i = 0; i < col1.length; i++) {
                Cell cell = rowHeader1.createCell(i);
                cell.setCellValue(col1[i]);
                cell.setCellStyle(headerStyle);
            }
            for (int i = 0; i < tabla.getRowCount(); i++) {
                Row row = sheet1.createRow(i + 1);
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    row.createCell(j).setCellValue(tabla.getValueAt(i, j).toString());
                }
            }
            for (int i = 0; i < 4; i++) sheet1.autoSizeColumn(i);

            // --- HOJA 2: DESGLOSE DETALLADO ---
            Sheet sheet2 = workbook.createSheet("Detalle por Producto");
            Row rowHeader2 = sheet2.createRow(0);
            String[] col2 = {"Folio", "Fecha", "Vendedor", "Código", "Producto", "Cant.", "Subtotal"};
            for (int i = 0; i < col2.length; i++) {
                Cell cell = rowHeader2.createCell(i);
                cell.setCellValue(col2[i]);
                cell.setCellStyle(headerStyle);
            }

            List<String[]> detalles = repo.obtenerDetallesParaReporte();
            for (int i = 0; i < detalles.size(); i++) {
                Row row = sheet2.createRow(i + 1);
                String[] datos = detalles.get(i);
                for (int j = 0; j < datos.length; j++) {
                    row.createCell(j).setCellValue(datos[j]);
                }
            }
            for (int i = 0; i < 7; i++) sheet2.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(archivo)) {
                workbook.write(out);
            }
            JOptionPane.showMessageDialog(this, "Reporte generado.");
            Desktop.getDesktop().open(archivo);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}