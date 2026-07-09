package ui;

import entities.Sesion;
import entities.Usuario;
import repository.UsuarioLocalImpl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import sync.SincronizadorNube;

public class Login extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private UsuarioLocalImpl repoUsuarios;
    private Point initialClick;

    public Login() {
        this.repoUsuarios = new UsuarioLocalImpl();
        setTitle("Refaccionaria Chayito - Autenticación");
        setSize(500, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setResizable(false);

        PanelFondo pnlFondo = new PanelFondo();
        pnlFondo.setLayout(new BorderLayout());
        setContentPane(pnlFondo);

        // Arrastre de ventana
        pnlFondo.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });
        pnlFondo.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + e.getX() - initialClick.x, getLocation().y + e.getY() - initialClick.y);
            }
        });

        // Botón de cierre superior
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlControles.setOpaque(false);
        JButton btnCerrar = new JButton("✕");
        btnCerrar.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnCerrar.setForeground(Color.GRAY);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusable(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> System.exit(0));
        pnlControles.add(btnCerrar);
        pnlFondo.add(pnlControles, BorderLayout.NORTH);

        // Contenedor vertical para las tarjetas flotantes
        JPanel pnlCardsContainer = new JPanel();
        pnlCardsContainer.setLayout(new BoxLayout(pnlCardsContainer, BoxLayout.Y_AXIS));
        pnlCardsContainer.setOpaque(false);

        // --- TARJETA 1: BIENVENIDA ---
        PanelTarjetaBienvenida pnlTarjetaBienvenido = new PanelTarjetaBienvenida();
        pnlTarjetaBienvenido.setLayout(new BoxLayout(pnlTarjetaBienvenido, BoxLayout.Y_AXIS));
        pnlTarjetaBienvenido.setPreferredSize(new Dimension(380, 140));
        pnlTarjetaBienvenido.setMaximumSize(new Dimension(380, 140));
        pnlTarjetaBienvenido.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlTarjetaBienvenido.setBorder(BorderFactory.createEmptyBorder(35, 20, 20, 20));

        JLabel lblSaludo = new JLabel("¡H O L A   D E   N U E V O!", SwingConstants.CENTER);
        lblSaludo.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSaludo.setForeground(new Color(255, 255, 255, 170));
        lblSaludo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMarca = new JLabel("Refaccionaria Chayito", SwingConstants.CENTER);
        lblMarca.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblMarca.setForeground(Color.WHITE);
        lblMarca.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlTarjetaBienvenido.add(lblSaludo);
        pnlTarjetaBienvenido.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlTarjetaBienvenido.add(lblMarca);

        // --- TARJETA 2: FORMULARIO DE ACCESO ---
        PanelTarjeta pnlTarjetaFormulario = new PanelTarjeta();
        pnlTarjetaFormulario.setLayout(new BoxLayout(pnlTarjetaFormulario, BoxLayout.Y_AXIS));
        pnlTarjetaFormulario.setPreferredSize(new Dimension(380, 350));
        pnlTarjetaFormulario.setMaximumSize(new Dimension(380, 350));
        pnlTarjetaFormulario.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlTarjetaFormulario.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel lblUser = new JLabel("👤 Usuario", SwingConstants.CENTER);
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblUser.setForeground(new Color(100, 116, 139));
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtUsuario = new JTextField(); 
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtUsuario.setHorizontalAlignment(JTextField.CENTER);
        txtUsuario.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JLabel lblPass = new JLabel("🔒 Contraseña", SwingConstants.CENTER);
        lblPass.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPass.setForeground(new Color(100, 116, 139));
        lblPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtPassword = new JPasswordField(); 
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPassword.setHorizontalAlignment(JTextField.CENTER);
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JButton btnEntrar = new JButton("Iniciar Sesión");
        btnEntrar.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnEntrar.setBackground(Color.decode("#0F43AB")); 
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlTarjetaFormulario.add(lblUser);
        pnlTarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlTarjetaFormulario.add(txtUsuario);
        pnlTarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 25)));
        pnlTarjetaFormulario.add(lblPass);
        pnlTarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlTarjetaFormulario.add(txtPassword);
        pnlTarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 40)));
        pnlTarjetaFormulario.add(btnEntrar);

        pnlCardsContainer.add(pnlTarjetaBienvenido);
        pnlCardsContainer.add(Box.createRigidArea(new Dimension(0, 20))); 
        pnlCardsContainer.add(pnlTarjetaFormulario);

        JPanel pnlCentro = new JPanel(new GridBagLayout());
        pnlCentro.setOpaque(false);
        pnlCentro.add(pnlCardsContainer);
        pnlFondo.add(pnlCentro, BorderLayout.CENTER);

        // Eventos
        btnEntrar.addActionListener(e -> validarCredenciales());
        txtUsuario.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(e -> validarCredenciales());
    }

    private void validarCredenciales() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa usuario y contraseña.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario u = repoUsuarios.autenticar(username, password);

        if (u != null) {
            Sesion.setUsuarioActual(u);
            
            // --- INICIAR SINCRONIZADOR AL LOGEAR ---
            new SincronizadorNube().iniciar();
            
            if (u.getRol().equals("Administrador")) {
                new DashboardAdmin().setVisible(true);
                this.dispose();
            } else {
                MenuVendedor mv = new MenuVendedor();
                mv.iniciarSistema(); 
                if (mv.isVisible()) this.dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Datos incorrectos.", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
        }
    }

    private class PanelTarjetaBienvenida extends JPanel {
        public PanelTarjetaBienvenida() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int shadowOffset = 4;
            int margin = 4;
            int w = getWidth() - (margin * 2);
            int h = getHeight() - (margin * 2) - shadowOffset;

            // Sombra simétrica
            g2.setColor(new Color(0, 0, 0, 15)); 
            g2.fillRoundRect(margin, margin + shadowOffset, w, h, 20, 20);
            
            // Cuerpo con Degradado
            GradientPaint gp = new GradientPaint(margin, margin, Color.decode("#0F43AB"), w, h, new Color(30, 41, 59));
            g2.setPaint(gp);
            g2.fillRoundRect(margin, margin, w, h, 20, 20);
            
            // Círculos recortados al contenedor
            g2.setClip(margin, margin, w, h);
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillOval(getWidth() - 60, -20, 100, 100);
            g2.fillOval(-30, getHeight() - 50, 80, 80);
            
            g2.dispose();
        }
    }

    private class PanelTarjeta extends JPanel {
        public PanelTarjeta() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int shadowOffset = 4;
            int margin = 4;
            int w = getWidth() - (margin * 2);
            int h = getHeight() - (margin * 2) - shadowOffset;

            // Sombra simétrica
            g2.setColor(new Color(0, 0, 0, 15)); 
            g2.fillRoundRect(margin, margin + shadowOffset, w, h, 20, 20);
            
            // Fondo Blanco centrado
            g2.setColor(Color.WHITE); 
            g2.fillRoundRect(margin, margin, w, h, 20, 20);
            
            g2.dispose();
        }
    }

    private class PanelFondo extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            
            g2.setColor(new Color(241, 245, 249)); 
            g2.fillRect(0, 0, w, h);
            
            g2.setColor(new Color(30, 41, 59));
            Path2D.Double p = new Path2D.Double();
            p.moveTo(0, 0); p.lineTo(w, 0); p.lineTo(w, h * 0.45); p.lineTo(0, h * 0.65); p.closePath();
            g2.fill(p);
            
            g2.setColor(Color.decode("#0F43AB"));
            Path2D.Double pLinea = new Path2D.Double();
            pLinea.moveTo(0, h * 0.65); pLinea.lineTo(w, h * 0.45); pLinea.lineTo(w, (h * 0.45) + 8); pLinea.lineTo(0, (h * 0.65) + 8); pLinea.closePath();
            g2.fill(pLinea);
            
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); 
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Button.arc", 15);
        } catch (Exception e) {}
        
        database.ConexionNube.cargarConfiguracionLocal();
        
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}