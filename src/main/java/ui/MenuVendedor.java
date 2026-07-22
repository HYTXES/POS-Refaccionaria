package ui;

import entities.Sesion;
import repository.CajaLocalImpl;
import repository.DashboardLocalImpl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

public class MenuVendedor extends JFrame {

    private CajaLocalImpl cajaRepo;
    private Point initialClick;

    public MenuVendedor() {
        Sesion.auditarAcceso(this, "CUALQUIERA");
        this.cajaRepo = new CajaLocalImpl();
        
        // --- CONFIGURACIÓN DE VENTANA ---
        setTitle("Punto de Venta - Refaccionaria Chayito");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        setUndecorated(true);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        // --- CABECERA ---
        PanelHeaderModerno pnlHeader = new PanelHeaderModerno();
        pnlHeader.setLayout(new BorderLayout());
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        // Arrastre
        pnlHeader.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });
        pnlHeader.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x; int thisY = getLocation().y;
                setLocation(thisX + e.getX() - initialClick.x, thisY + e.getY() - initialClick.y);
            }
        });

        // Títulos
        JPanel pnlTitulos = new JPanel(new GridLayout(2, 1));
        pnlTitulos.setOpaque(false);
        JLabel lblTitulo = new JLabel("Terminal de Venta Rápida");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel lblSubtitulo = new JLabel("👤 Usuario: " + (Sesion.getUsuarioActual() != null ? Sesion.getUsuarioActual().getUsername() : "Cajero"));
        lblSubtitulo.setForeground(new Color(148, 163, 184));
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pnlTitulos.add(lblTitulo); 
        pnlTitulos.add(lblSubtitulo);
        pnlHeader.add(pnlTitulos, BorderLayout.WEST);

        // Controles derecha
        JPanel pnlEast = new JPanel(new BorderLayout(0, 15));
        pnlEast.setOpaque(false);

        JPanel pnlControlesVentana = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlControlesVentana.setOpaque(false);
        JButton btnMinimizar = crearBotonVentana("—"); 
        btnMinimizar.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));
        
        JButton btnMaximizar = crearBotonVentana("◻"); 
        btnMaximizar.addActionListener(e -> {
            if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) { 
                setExtendedState(JFrame.NORMAL); setSize(1000, 700); setLocationRelativeTo(null); 
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });
        
        JButton btnCerrarVentana = crearBotonVentana("✕");
        btnCerrarVentana.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Cerrar todo el sistema sin hacer corte de caja?", "Advertencia Crítica", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });
        
        pnlControlesVentana.add(btnMinimizar); 
        pnlControlesVentana.add(btnMaximizar); 
        pnlControlesVentana.add(btnCerrarVentana);

        JPanel pnlBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotonesAccion.setOpaque(false);
        JButton btnCorteCaja = new JButton("💵 Corte de Caja"); 
        btnCorteCaja.setBackground(new Color(46, 204, 113)); 
        btnCorteCaja.setForeground(Color.WHITE); 
        btnCorteCaja.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCorteCaja.setFocusPainted(false);
        btnCorteCaja.setFocusable(false);
        btnCorteCaja.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnLogout = new JButton("🚪 Cerrar Sesión"); 
        btnLogout.setBackground(new Color(220, 38, 38)); 
        btnLogout.setForeground(Color.WHITE); 
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogout.setFocusPainted(false);
        btnLogout.setFocusable(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlBotonesAccion.add(btnCorteCaja); 
        pnlBotonesAccion.add(btnLogout);
        
        pnlEast.add(pnlControlesVentana, BorderLayout.NORTH);
        pnlEast.add(pnlBotonesAccion, BorderLayout.SOUTH);
        pnlHeader.add(pnlEast, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- CONTENEDOR DE PESTAÑAS ---
        UIManager.put("TabbedPane.underlineColor", Color.decode("#0F43AB"));
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 16));
        tabbedPane.setFocusable(false);
        SwingUtilities.updateComponentTreeUI(tabbedPane);

        tabbedPane.addTab("🛒 Punto de Venta", new CajaPanel());
        tabbedPane.addTab("🔍 Consultar Catálogo", new ConsultaProductoPanel());
        tabbedPane.addTab("☁️ Otras Sucursales", new InventarioGlobalPanel());
        
        add(tabbedPane, BorderLayout.CENTER);

        // --- EVENTOS ---
        btnLogout.addActionListener(e -> { 
            int confirm = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión sin hacer corte de caja?", "Aviso", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) cerrarSesionSegura(); 
        });
        btnCorteCaja.addActionListener(e -> procesarCorteCaja());
    }

    // --- LÓGICA DE INICIACIÓN SEGURA ---
    public void iniciarSistema() {
        if (cajaRepo.hayCajaAbierta()) {
            double fondoActual = cajaRepo.obtenerFondoInicial();
            JOptionPane.showMessageDialog(null, 
                "Reanudando turno de caja existente.\nFondo registrado en sistema: $" + String.format("%.2f", fondoActual), 
                "Turno en Curso", 
                JOptionPane.INFORMATION_MESSAGE);
            this.setVisible(true);
        } else {
            if (ejecutarValidacionCaja()) {
                this.setVisible(true);
            }
        }
    }

    private boolean ejecutarValidacionCaja() {
        while (true) {
            String input = JOptionPane.showInputDialog(null, "🔒 APERTURA DE TURNO\nIngresa el fondo inicial en caja (efectivo base):", "0.00");
            if (input == null) return false;
            try {
                double fondo = Double.parseDouble(input);
                if (fondo < 0) throw new NumberFormatException();
                
                String vendedor = (Sesion.getUsuarioActual() != null) ? Sesion.getUsuarioActual().getUsername() : "Usuario de Prueba";
                cajaRepo.abrirCaja(vendedor, fondo);
                JOptionPane.showMessageDialog(null, "Caja abierta exitosamente con $" + fondo);
                return true;
            } catch (NumberFormatException ex) { 
                JOptionPane.showMessageDialog(null, "Por favor ingresa una cantidad numérica válida y positiva.", "Entrada Inválida", JOptionPane.WARNING_MESSAGE); 
            }
        }
    }

    private void procesarCorteCaja() {
        while (true) {
            String input = JOptionPane.showInputDialog(this, "💵 CORTE DE CAJA (Cierre Ciego)\nCuenta los billetes y monedas.\n¿Cuánto dinero físico EXACTO hay en el cajón?", "0.00");
            if (input == null) return; 
            
            try {
                double totalRealCajon = Double.parseDouble(input);
                if (totalRealCajon < 0) throw new NumberFormatException();

                double fondo = cajaRepo.obtenerFondoInicial();
                double ventas = cajaRepo.calcularVentasTurno();
                DashboardLocalImpl dashRepo = new DashboardLocalImpl();
                String fechaApertura = cajaRepo.obtenerFechaAperturaActual();
                double retiros = dashRepo.calcularRetirosTurnoActual(fechaApertura);

                double totalEsperado = fondo + ventas - retiros; 
                double diferencia = totalRealCajon - totalEsperado;

                String estadoCorte = (diferencia == 0) ? "EXCELENTE (Cuadrado)" 
                                   : (diferencia > 0) ? "SOBRANTE" 
                                   : "FALTANTE";

                String mensaje = String.format("""
                    📊 RESUMEN DE CORTE:
                    
                    Fondo Inicial: $%.2f
                    + Ventas del Turno: $%.2f
                    - Retiros Físicos: $%.2f
                    = Total Esperado en Sistema: $%.2f
                    ---------------------------------------
                    Total Real (Declarado): $%.2f
                    Diferencia: $%.2f (%s)
                    
                    ¿Confirmar cierre de caja?
                    """, fondo, ventas, retiros, totalEsperado, totalRealCajon, diferencia, estadoCorte);
                
                int confirm = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar Corte", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (cajaRepo.cerrarCaja(totalRealCajon)) {
                        String vendedor = (Sesion.getUsuarioActual() != null) ? Sesion.getUsuarioActual().getUsername() : "Usuario";
                        util.ReporteCajaGenerator.generarTxtCorte(vendedor, fondo, ventas, totalEsperado, totalRealCajon, diferencia, estadoCorte);
                        JOptionPane.showMessageDialog(this, "Corte registrado correctamente.\nEl reporte TXT fue generado.\nCerrando sesión...", "Corte Exitoso", JOptionPane.INFORMATION_MESSAGE);
                        cerrarSesionSegura();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al guardar el corte en la base de datos.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Monto inválido. Usa solo números y puntos decimales.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cerrarSesionSegura() {
        Sesion.setUsuarioActual(null);
        this.dispose();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }

    private JButton crearBotonVentana(String texto) {
        JButton btn = new JButton(texto); 
        btn.setFont(new Font("SansSerif", Font.BOLD, 18)); 
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); 
        btn.setBorderPainted(false); 
        btn.setFocusable(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private class PanelHeaderModerno extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // --- DETECCIÓN DINÁMICA DE TEMA PARA EL HEADER ---
            boolean isDark = UIManager.getBoolean("laf.dark");
            Color colorFondoHeader = isDark ? new Color(15, 23, 42) : new Color(30, 41, 59);

            g2d.setColor(colorFondoHeader);
            g2d.fillRect(0, 0, w, h);

            g2d.setColor(Color.decode("#0F43AB"));
            Path2D.Double poly = new Path2D.Double();
            poly.moveTo(w - 400, 0); 
            poly.lineTo(w, 0);        
            poly.lineTo(w, h);        
            poly.lineTo(w - 450, h); 
            poly.closePath();
            g2d.fill(poly);
            
            GradientPaint shadow = new GradientPaint(0, h - 5, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 50));
            g2d.setPaint(shadow);
            g2d.fillRect(0, h - 5, w, 5);
            g2d.dispose();
        }
    }
}