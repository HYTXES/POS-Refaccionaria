package ui;

import entities.Sesion;
import repository.CajaLocalImpl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;

public class DashboardAdmin extends JFrame {

    private Point initialClick;
    private CajaLocalImpl cajaRepo;

    public DashboardAdmin() {
        // --- 1. CONFIGURACIÓN VISUAL (DEBE IR ANTES DE TODO) ---
        UIManager.put("TabbedPane.underlineColor", Color.decode("#0F43AB"));
        UIManager.put("TabbedPane.selectedUnderlineColor", Color.decode("#0F43AB"));
        UIManager.put("TabbedPane.focusColor", Color.decode("#0F43AB"));

        // --- AUDITORÍA DE SEGURIDAD ESTRICTA ---
        Sesion.auditarAcceso(this, "Administrador");

        this.cajaRepo = new CajaLocalImpl();

        setTitle("Refaccionaria Chayito - Administración");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setUndecorated(true);

        PanelHeaderModerno pnlHeader = new PanelHeaderModerno();
        pnlHeader.setLayout(new BorderLayout());
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        pnlHeader.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        pnlHeader.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        String nombreUsuario = (Sesion.getUsuarioActual() != null) ? Sesion.getUsuarioActual().getUsername() : "Administrador";
        JPanel pnlTitulos = new JPanel(new GridLayout(2, 1));
        pnlTitulos.setOpaque(false); 
        
        JLabel lblTitulo = new JLabel("Panel de Control Administrativo");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        JLabel lblSubtitulo = new JLabel("👤 Usuario activo: " + nombreUsuario);
        lblSubtitulo.setForeground(new Color(148, 163, 184)); 
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        pnlTitulos.add(lblTitulo);
        pnlTitulos.add(lblSubtitulo);
        pnlHeader.add(pnlTitulos, BorderLayout.WEST);

        JPanel pnlEast = new JPanel(new BorderLayout(0, 15));
        pnlEast.setOpaque(false);

        JPanel pnlControlesVentana = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlControlesVentana.setOpaque(false);

        JButton btnMinimizar = crearBotonVentana("—");
        btnMinimizar.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        JButton btnMaximizar = crearBotonVentana("◻");
        btnMaximizar.addActionListener(e -> {
            if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
                setSize(1000, 700);
                setLocationRelativeTo(null);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });

        JButton btnCerrarVentana = crearBotonVentana("✕");
        btnCerrarVentana.addActionListener(e -> System.exit(0)); 

        pnlControlesVentana.add(btnMinimizar);
        pnlControlesVentana.add(btnMaximizar);
        pnlControlesVentana.add(btnCerrarVentana);

        JPanel pnlBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotonesAccion.setOpaque(false);

        JButton btnRetiro = new JButton("💸 Retirar Efectivo");
        btnRetiro.setBackground(new Color(245, 158, 11)); 
        btnRetiro.setForeground(Color.WHITE);
        btnRetiro.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnRetiro.setFocusPainted(false);
        btnRetiro.setFocusable(false);
        btnRetiro.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

        pnlBotonesAccion.add(btnRetiro);
        pnlBotonesAccion.add(btnCorteCaja);
        pnlBotonesAccion.add(btnLogout);

        btnRetiro.addActionListener(e -> {
            if (!cajaRepo.hayCajaAbierta()) {
                JOptionPane.showMessageDialog(this, "Debe haber una caja abierta por el vendedor para registrar un retiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new DialogoRetiro(this).setVisible(true);
        });

        btnCorteCaja.addActionListener(e -> procesarCorteCaja());
        
        btnLogout.addActionListener(e -> {
            Sesion.setUsuarioActual(null);
            new Login().setVisible(true);
            this.dispose();
        });

        pnlEast.add(pnlControlesVentana, BorderLayout.NORTH);
        pnlEast.add(pnlBotonesAccion, BorderLayout.SOUTH);
        pnlHeader.add(pnlEast, BorderLayout.EAST);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 16));
        tabbedPane.setFocusable(false); 

        tabbedPane.addTab("🏠 Inicio", new InicioPanel());
        tabbedPane.addTab("🛒 Punto de Venta", new CajaPanel());
        tabbedPane.addTab("📦 Inventario Local", new InventarioPanel());
        tabbedPane.addTab("🚚 Proveedores", new ProveedoresPanel());
        tabbedPane.addTab("☁️ Otras Sucursales", new InventarioGlobalPanel()); 
        tabbedPane.addTab("📊 Historial de Ventas", new HistorialVentasPanel());
        tabbedPane.addTab("👥 Usuarios", new UsuariosPanel());
        tabbedPane.addTab("💵 Cortes de Caja", new CortesCajaPanel());
        tabbedPane.addTab("⚙️ Configuración", new ConfiguracionPanel());

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 3) {
                UIManager.put("TabbedPane.underlineColor", new Color(15, 67, 171, 100)); 
            } else {
                UIManager.put("TabbedPane.underlineColor", Color.decode("#0F43AB"));
            }
            SwingUtilities.updateComponentTreeUI(tabbedPane);
        });

        add(pnlHeader, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void iniciarSistema() {
        if (cajaRepo.hayCajaAbierta()) {
            double fondoActual = cajaRepo.obtenerFondoInicial();
            JOptionPane.showMessageDialog(null, 
                "Turno en curso.\nFondo registrado en sistema: $" + String.format("%.2f", fondoActual), 
                "Información de Caja", 
                JOptionPane.INFORMATION_MESSAGE);
            this.setVisible(true);
        } else {
            int opcion = JOptionPane.showConfirmDialog(null,
                "La caja actualmente está CERRADA.\n¿Deseas abrir el turno de caja con fondo inicial ahora para poder cobrar ventas?",
                "Apertura de Caja",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (opcion == JOptionPane.YES_OPTION) {
                if (ejecutarValidacionCaja()) {
                    this.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Entrando al sistema en Modo Solo-Lectura.\nNo podrás realizar ventas hasta que inicies turno.", "Modo Administrativo", JOptionPane.WARNING_MESSAGE);
                    this.setVisible(true);
                }
            } else {
                this.setVisible(true);
            }
        }
    }

    private boolean ejecutarValidacionCaja() {
        while (true) {
            String input = JOptionPane.showInputDialog(null, "🔒 APERTURA DE TURNO (ADMIN)\nIngresa el fondo inicial en caja (efectivo base):", "0.00");
            if (input == null) return false;
            try {
                double fondo = Double.parseDouble(input);
                if (fondo < 0) throw new NumberFormatException();
                
                String vendedor = (Sesion.getUsuarioActual() != null) ? Sesion.getUsuarioActual().getUsername() : "Administrador";
                cajaRepo.abrirCaja(vendedor, fondo);
                JOptionPane.showMessageDialog(null, "Caja abierta exitosamente con $" + String.format("%.2f", fondo));
                return true;
            } catch (NumberFormatException ex) { 
                JOptionPane.showMessageDialog(null, "Por favor ingresa una cantidad numérica válida y positiva.", "Entrada Inválida", JOptionPane.WARNING_MESSAGE); 
            }
        }
    }

    private void procesarCorteCaja() {
        if (!cajaRepo.hayCajaAbierta()) {
            JOptionPane.showMessageDialog(this, "La caja ya está cerrada o el vendedor aún no ha iniciado turno hoy.\nNo hay información pendiente de corte.", "Aviso del Sistema", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        while (true) {
            String input = JOptionPane.showInputDialog(this, 
                "💵 CORTE DE CAJA (MODO ADMINISTRADOR)\nSe cerrará y validará el turno que el cajero dejó abierto.\n¿Cuánto dinero físico EXACTO hay en el cajón?", "0.00");
            
            if (input == null) return; 
            
            try {
                double totalRealCajon = Double.parseDouble(input);
                if (totalRealCajon < 0) throw new NumberFormatException();

                double fondo = cajaRepo.obtenerFondoInicial();
                double ventas = cajaRepo.calcularVentasTurno();
                repository.DashboardLocalImpl dashRepo = new repository.DashboardLocalImpl();
                String fechaApertura = cajaRepo.obtenerFechaAperturaActual();
                double retiros = dashRepo.calcularRetirosTurnoActual(fechaApertura);

                double totalEsperado = fondo + ventas - retiros; 
                double diferencia = totalRealCajon - totalEsperado;

                String estadoCorte = (diferencia == 0) ? "EXCELENTE (Cuadrado)" 
                                   : (diferencia > 0) ? "SOBRANTE" 
                                   : "FALTANTE";

                String mensaje = String.format("""
                    📊 RESUMEN DE CORTE DE CAJA (ADMIN):
                    
                    Fondo Inicial: $%.2f
                    + Ventas del Turno: $%.2f
                    - Retiros Físicos: $%.2f
                    = Total Esperado en Sistema: $%.2f
                    ---------------------------------------
                    Total Real (Declarado): $%.2f
                    Diferencia: $%.2f (%s)
                    
                    ¿Confirmar cierre definitivo de la caja?
                    """, fondo, ventas, retiros, totalEsperado, totalRealCajon, diferencia, estadoCorte);

                int confirm = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar Corte", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (cajaRepo.cerrarCaja(totalRealCajon)) {
                        String vendedor = (Sesion.getUsuarioActual() != null) ? Sesion.getUsuarioActual().getUsername() : "Administrador";
                        util.ReporteCajaGenerator.generarTxtCorte(vendedor, fondo, ventas, totalEsperado, totalRealCajon, diferencia, estadoCorte);
                        JOptionPane.showMessageDialog(this, "Turno cerrado correctamente. El reporte TXT fue generado en la carpeta 'Cortes_Caja'.\nEl sistema está listo para un nuevo turno.", "Corte Exitoso", JOptionPane.INFORMATION_MESSAGE);
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