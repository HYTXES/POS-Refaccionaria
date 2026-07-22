package ui;

import repository.DashboardLocalImpl;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class InicioPanel extends JPanel {

    private DashboardLocalImpl repo;
    
    // Labels financieros y de operacion
    private JLabel lblVentasTotales, lblRetiros, lblOperaciones;
    // Labels de inventario
    private JLabel lblArticulosVendidos, lblEntradas, lblBajoStock;

    public InicioPanel() {
        this.repo = new DashboardLocalImpl();
        
        // --- DETECCIÓN DE TEMA MODO OSCURO ---
        boolean isDark = UIManager.getBoolean("laf.dark");
        Color bgFondo = isDark ? new Color(15, 23, 42) : new Color(241, 245, 249);
        Color txtPrincipal = isDark ? Color.WHITE : new Color(30, 41, 59);

        setLayout(new BorderLayout(20, 20));
        setBackground(bgFondo);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // --- HEADER CON TÍTULO Y BOTÓN DE RECARGA ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("📊 Resumen Operativo General (Hoy)");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(txtPrincipal); // Color dinámico
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JButton btnActualizar = new JButton("🔄 Actualizar Datos");
        btnActualizar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnActualizar.setBackground(new Color(15, 67, 171)); // Azul
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnActualizar.addActionListener(e -> cargarKPIs());
        pnlHeader.add(btnActualizar, BorderLayout.EAST);
        
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlTarjetas = new JPanel(new GridLayout(2, 3, 25, 25));
        pnlTarjetas.setOpaque(false);

        // Instanciar los labels
        lblVentasTotales = new JLabel("$0.00");
        lblOperaciones = new JLabel("0");
        lblRetiros = new JLabel("$0.00");
        
        lblArticulosVendidos = new JLabel("0");
        lblEntradas = new JLabel("0");
        lblBajoStock = new JLabel("0");

        // Fila 1: Finanzas y operaciones Base
        pnlTarjetas.add(crearTarjeta("Ingresos Totales", lblVentasTotales, "#0F43AB", "💵", isDark)); 
        pnlTarjetas.add(crearTarjeta("Tickets Emitidos", lblOperaciones, "#16A34A", "🧾", isDark)); 
        pnlTarjetas.add(crearTarjeta("Retiros de Caja", lblRetiros, "#DC2626", "💸", isDark)); 

        // Fila 2: Flujo de inventario
        pnlTarjetas.add(crearTarjeta("Artículos Vendidos", lblArticulosVendidos, "#8B5CF6", "📦", isDark)); 
        pnlTarjetas.add(crearTarjeta("Entradas de Mercancía", lblEntradas, "#0284C7", "🚚", isDark)); 
        pnlTarjetas.add(crearTarjeta("Alertas de Bajo Stock", lblBajoStock, "#F59E0B", "⚠️", isDark)); 

        add(pnlTarjetas, BorderLayout.CENTER);

        // Intento de recargar datos automaticamente
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                cargarKPIs();
            }
        });

        // Carga inicial
        cargarKPIs();
    }

    private void cargarKPIs() {
        System.out.println("🔄 Consultando base de datos para actualizar KPIs...");
        
        // Consultas financieras
        double ventas = repo.getVentasTotalesHoy();
        double retiros = repo.getRetirosTotalesHoy();
        int ops = repo.getCantidadVentasHoy();
        
        // Consultas de Inventario
        int artsVendidos = repo.getArticulosVendidosHoy();
        int entradas = repo.getEntradasInventarioHoy();
        int bajoStock = repo.getProductosBajoStock();

        System.out.println("Ventas hoy BD: $" + ventas);
        System.out.println("Operaciones hoy BD: " + ops);

        lblVentasTotales.setText(String.format("$%.2f", ventas));
        lblOperaciones.setText(String.valueOf(ops));
        lblRetiros.setText(String.format("$%.2f", retiros));
        
        lblArticulosVendidos.setText(String.valueOf(artsVendidos));
        lblEntradas.setText(String.valueOf(entradas));
        lblBajoStock.setText(String.valueOf(bajoStock));
    }

    // Método para dibujar las tarjetas con sombra redondeada y un ícono en el título
    private JPanel crearTarjeta(String titulo, JLabel lblValor, String colorHex, String emoji, boolean isDark) {
        JPanel tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra suave adaptada al tema
                g2d.setColor(isDark ? new Color(0, 0, 0, 40) : new Color(0, 0, 0, 10));
                g2d.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 6, 20, 20);
                
                // Fondo de la tarjeta (Blanco de día, Gris Azulado oscuro de noche)
                g2d.setColor(isDark ? new Color(30, 41, 59) : Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 8, 20, 20);
                
                // Línea superior de color distintivo
                g2d.setColor(Color.decode(colorHex));
                g2d.setClip(new RoundRectangle2D.Double(0, 0, getWidth() - 6, 10, 20, 20));
                g2d.fillRect(0, 0, getWidth(), 10);
                
                g2d.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 20));

        // Contenedor del título
        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTitulo.setOpaque(false);
        pnlTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblIcono = new JLabel(emoji);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTit.setForeground(isDark ? new Color(148, 163, 184) : new Color(100, 116, 139)); // Gris adaptado
        
        pnlTitulo.add(lblIcono);
        pnlTitulo.add(lblTit);
        
        // Estilización del valor grande
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 42));
        lblValor.setForeground(Color.decode(colorHex));
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValor.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        tarjeta.add(pnlTitulo);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 15)));
        tarjeta.add(lblValor);

        return tarjeta;
    }
}