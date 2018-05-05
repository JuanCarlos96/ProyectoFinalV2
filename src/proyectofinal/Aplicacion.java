/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectofinal;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.mysql.jdbc.Connection;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author juancarlos
 */
public class Aplicacion extends javax.swing.JFrame {
    private Conector con;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private Date date = new Date();
    private BufferedImage image = null;
    private byte[] imageblob = null;
    private File imagen_seleccionada = null;

    /**
     * Creates new form Aplicacion
     */
    public Aplicacion() {
        initComponents();
    }
    
    private boolean admin() {
        boolean esAdmin = false;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("administrador.dat"));
            String linea;
            try{
                while (true) {
                    linea=dis.readUTF();
                    //System.out.println(linea);
                    if (txtUser.getText().trim().equals(linea.split(" ")[0]) && String.valueOf(txtPassword.getPassword()).equals(linea.split(" ")[1])) {
                        esAdmin = true;
                    }
                }
            } catch (EOFException eofe) {
            }
            dis.close();
        } catch (IOException e) {
            System.out.println("Error leyendo el archivo administrador.dat");
            e.printStackTrace();
        }
        return esAdmin;
    }
    
    private boolean usuario() {
        boolean esUsuario = false;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("usuarios.dat"));
            String linea;
            try{
                while (true) {
                    linea=dis.readUTF();
                    //System.out.println(linea);
                    if (txtUser.getText().trim().equals(linea.split(" ")[0]) && String.valueOf(txtPassword.getPassword()).equals(linea.split(" ")[1])) {
                        esUsuario = true;
                    }
                }
            } catch (EOFException eofe) {
            }
            dis.close();
        } catch (IOException e) {
            System.out.println("Error leyendo el archivo usuarios.dat");
            e.printStackTrace();
        }
        return esUsuario;
    }
    
    private int calculatePasswordStrength(String password){
        
        //total score of password
        int iPasswordScore = 0;
        
        if (password.length()<8)
            return 0;
        else if (password.length()>=10)
            iPasswordScore += 2;
        else 
            iPasswordScore += 1;
        
        //if it contains one digit, add 2 to total score
        if (password.matches("(?=.*[0-9]).*"))
            iPasswordScore += 2;
        
        //if it contains one lower case letter, add 2 to total score
        if (password.matches("(?=.*[a-z]).*"))
            iPasswordScore += 2;
        
        //if it contains one upper case letter, add 2 to total score
        if (password.matches("(?=.*[A-Z]).*"))
            iPasswordScore += 2;    
        
        //if it contains one special character, add 2 to total score
        if (password.matches("(?=.*[~!@#$%^&*()_-]).*"))
            iPasswordScore += 2;
        
        return iPasswordScore;
    }
    
    private void listarUsuarios() {
        DefaultTableModel modeloUsuarios = (DefaultTableModel) tablaUsuarios.getModel();
        ArrayList<Departamento> usuarios = con.listaUsuarios();
        
        for (int i=modeloUsuarios.getRowCount()-1; i>=0; i--) {
            modeloUsuarios.removeRow(i);
        }
        
        for (Departamento d:usuarios) {
            modeloUsuarios.addRow(new Object[] {d.getUsuario(), d.getClave()});
        }
        
        btnModUsuario.setEnabled(false);
        btnEliminarUsuario.setEnabled(false);
    }
    
    private void listarNoticiasAdmin() {
        DefaultTableModel modeloTodasNoticias = (DefaultTableModel) tablaTodasNoticias.getModel();
        ArrayList<Noticia> noticias = con.listaNoticiasAdmin();
        
        for (int i=modeloTodasNoticias.getRowCount()-1; i>=0; i--) {
            modeloTodasNoticias.removeRow(i);
        }
        
        for (Noticia n:noticias) {
            String vigente = n.getVigente()==0? "No":"Sí";
            String publica = n.getPublica()==0? "No":"Sí";
            modeloTodasNoticias.addRow(new Object[] {n.getIdNoticia(), n.getDepartamento(), n.getFecha(), n.getDiasVigencia(), vigente, publica});
        }
        
        btnEditarNoticia.setEnabled(false);
        btnEliminarNoticia.setEnabled(false);
    }
    
    private void listarNoticiasUser() {
        DefaultTableModel modeloNoticias = (DefaultTableModel) jTable1.getModel();
        ArrayList<Noticia> noticias = con.listaNoticiasUser(txtUser.getText());
        
        for (int i=modeloNoticias.getRowCount()-1; i>=0; i--) {
            modeloNoticias.removeRow(i);
        }
        
        for (Noticia n:noticias) {
            modeloNoticias.addRow(new Object[] {n.getIdNoticia(), n.getFecha(), n.getDiasVigencia()});
        }
    }
    
    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        //Una función para reescalar una imagen. Se puede reutilizar tal cual
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }
    
    private BufferedImage writeImage(BufferedImage image, String texto, int x, int y) {
        Graphics g = image.getGraphics();
        g.setFont(g.getFont().deriveFont(30f));
        g.drawString(texto, x, y);
        g.dispose();
        return resize(image, 600, 400);// Se reescala para la vista previa
    }
    
    private boolean positionSelected() {
        return buttonGroup1.getSelection()!=null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainAplicacion = new javax.swing.JFrame();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        panelAdmin = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        btnUsuariosAdmin = new javax.swing.JButton();
        btnNoticiasAdmin = new javax.swing.JButton();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        panelUsuarios = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaUsuarios = new javax.swing.JTable();
        btnEliminarUsuario = new javax.swing.JButton();
        btnModUsuario = new javax.swing.JButton();
        btnNuevoUsuario = new javax.swing.JButton();
        panelNoticias = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaTodasNoticias = new javax.swing.JTable();
        btnEliminarNoticia = new javax.swing.JButton();
        btnEditarNoticia = new javax.swing.JButton();
        panelUsuario = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnNuevaNoticia = new javax.swing.JButton();
        btnVerNoticia = new javax.swing.JButton();
        adminMenuBar = new javax.swing.JMenuBar();
        archivoMenu = new javax.swing.JMenu();
        logout = new javax.swing.JMenuItem();
        salirMenuItem = new javax.swing.JMenuItem();
        peticionIP = new javax.swing.JDialog();
        jLabel3 = new javax.swing.JLabel();
        txtIP = new javax.swing.JTextField();
        btnConectarBD = new javax.swing.JButton();
        nuevoUsuario = new javax.swing.JDialog();
        jLabel4 = new javax.swing.JLabel();
        txtNuevoUsuario = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtNuevaClave = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        lblFortaleza = new javax.swing.JLabel();
        btnCancelarNuevoUsuario = new javax.swing.JButton();
        btnAceptarNuevoUsuario = new javax.swing.JButton();
        editarUsuario = new javax.swing.JDialog();
        jLabel8 = new javax.swing.JLabel();
        txtEditarClave = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        lblFortaleza2 = new javax.swing.JLabel();
        btnCancelarEditarUsuario = new javax.swing.JButton();
        btnAceptarEditarUsuario = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        lblClave = new javax.swing.JLabel();
        nuevaNoticia = new javax.swing.JDialog();
        jLabel12 = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lblDepartamento = new javax.swing.JLabel();
        btnImagen = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel15 = new javax.swing.JLabel();
        txtVigencia = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        topLeft = new javax.swing.JRadioButton();
        topRight = new javax.swing.JRadioButton();
        bottomLeft = new javax.swing.JRadioButton();
        bottomRight = new javax.swing.JRadioButton();
        middleLeft = new javax.swing.JRadioButton();
        middleRight = new javax.swing.JRadioButton();
        bottomCenter = new javax.swing.JRadioButton();
        topCenter = new javax.swing.JRadioButton();
        middleCenter = new javax.swing.JRadioButton();
        btnVistaPrevia = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        btnCancelarNuevaNoticia = new javax.swing.JButton();
        btnAceptarNuevaNoticia = new javax.swing.JButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        vistaPrevia = new javax.swing.JDialog();
        panelVistaPrevia = new javax.swing.JPanel();
        jFileChooser1 = new javax.swing.JFileChooser();
        editarNoticia = new javax.swing.JDialog();
        jLabel7 = new javax.swing.JLabel();
        txtIdNot = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtDepartamento = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txtFecha = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        txtRutaImagen = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        txtDiasVigencia = new javax.swing.JLabel();
        chkVigente = new javax.swing.JCheckBox();
        chkPublica = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        txtUser = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();
        txtPassword = new javax.swing.JPasswordField();

        mainAplicacion.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        mainAplicacion.setResizable(false);
        mainAplicacion.setSize(new java.awt.Dimension(682, 464));
        mainAplicacion.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                mainAplicacionWindowClosing(evt);
            }
        });

        jToolBar1.setFloatable(false);
        jToolBar1.setEnabled(false);

        btnUsuariosAdmin.setText("Usuarios");
        btnUsuariosAdmin.setFocusable(false);
        btnUsuariosAdmin.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUsuariosAdmin.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnUsuariosAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosAdminActionPerformed(evt);
            }
        });
        jToolBar1.add(btnUsuariosAdmin);

        btnNoticiasAdmin.setText("Noticias");
        btnNoticiasAdmin.setFocusable(false);
        btnNoticiasAdmin.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNoticiasAdmin.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNoticiasAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNoticiasAdminActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNoticiasAdmin);

        tablaUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Usuario", "Contraseña"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaUsuarios.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getTableHeader().setReorderingAllowed(false);
        tablaUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaUsuariosMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tablaUsuarios);
        if (tablaUsuarios.getColumnModel().getColumnCount() > 0) {
            tablaUsuarios.getColumnModel().getColumn(0).setResizable(false);
            tablaUsuarios.getColumnModel().getColumn(1).setResizable(false);
        }

        btnEliminarUsuario.setText("Eliminar");
        btnEliminarUsuario.setEnabled(false);
        btnEliminarUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarUsuarioActionPerformed(evt);
            }
        });

        btnModUsuario.setText("Modificar");
        btnModUsuario.setEnabled(false);
        btnModUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModUsuarioActionPerformed(evt);
            }
        });

        btnNuevoUsuario.setText("Nuevo");
        btnNuevoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoUsuarioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelUsuariosLayout = new javax.swing.GroupLayout(panelUsuarios);
        panelUsuarios.setLayout(panelUsuariosLayout);
        panelUsuariosLayout.setHorizontalGroup(
            panelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
            .addGroup(panelUsuariosLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnNuevoUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnModUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEliminarUsuario))
        );
        panelUsuariosLayout.setVerticalGroup(
            panelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUsuariosLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEliminarUsuario)
                    .addComponent(btnModUsuario)
                    .addComponent(btnNuevoUsuario)))
        );

        tablaTodasNoticias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id Noticia", "Departamento", "Fecha", "Días de vigencia", "Vigente", "Pública"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaTodasNoticias.getTableHeader().setReorderingAllowed(false);
        tablaTodasNoticias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaTodasNoticiasMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(tablaTodasNoticias);
        if (tablaTodasNoticias.getColumnModel().getColumnCount() > 0) {
            tablaTodasNoticias.getColumnModel().getColumn(0).setResizable(false);
            tablaTodasNoticias.getColumnModel().getColumn(1).setResizable(false);
            tablaTodasNoticias.getColumnModel().getColumn(2).setResizable(false);
            tablaTodasNoticias.getColumnModel().getColumn(3).setResizable(false);
            tablaTodasNoticias.getColumnModel().getColumn(4).setResizable(false);
            tablaTodasNoticias.getColumnModel().getColumn(5).setResizable(false);
        }

        btnEliminarNoticia.setText("Eliminar");
        btnEliminarNoticia.setEnabled(false);
        btnEliminarNoticia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarNoticiaActionPerformed(evt);
            }
        });

        btnEditarNoticia.setText("Ver");
        btnEditarNoticia.setEnabled(false);
        btnEditarNoticia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarNoticiaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelNoticiasLayout = new javax.swing.GroupLayout(panelNoticias);
        panelNoticias.setLayout(panelNoticiasLayout);
        panelNoticiasLayout.setHorizontalGroup(
            panelNoticiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNoticiasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnEditarNoticia)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEliminarNoticia))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
        );
        panelNoticiasLayout.setVerticalGroup(
            panelNoticiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNoticiasLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelNoticiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEliminarNoticia)
                    .addComponent(btnEditarNoticia)))
        );

        jLayeredPane1.setLayer(panelUsuarios, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(panelNoticias, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelNoticias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelNoticias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout panelAdminLayout = new javax.swing.GroupLayout(panelAdmin);
        panelAdmin.setLayout(panelAdminLayout);
        panelAdminLayout.setHorizontalGroup(
            panelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
            .addGroup(panelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLayeredPane1))
        );
        panelAdminLayout.setVerticalGroup(
            panelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAdminLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 417, Short.MAX_VALUE))
            .addGroup(panelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelAdminLayout.createSequentialGroup()
                    .addGap(0, 42, Short.MAX_VALUE)
                    .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id Noticia", "Fecha", "Días de vigencia"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTable1MouseReleased(evt);
            }
        });
        jScrollPane3.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        btnNuevaNoticia.setText("Nueva");
        btnNuevaNoticia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaNoticiaActionPerformed(evt);
            }
        });

        btnVerNoticia.setText("Ver");
        btnVerNoticia.setEnabled(false);
        btnVerNoticia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerNoticiaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelUsuarioLayout = new javax.swing.GroupLayout(panelUsuario);
        panelUsuario.setLayout(panelUsuarioLayout);
        panelUsuarioLayout.setHorizontalGroup(
            panelUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelUsuarioLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnNuevaNoticia)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnVerNoticia)))
                .addContainerGap())
        );
        panelUsuarioLayout.setVerticalGroup(
            panelUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevaNoticia)
                    .addComponent(btnVerNoticia))
                .addContainerGap())
        );

        jLayeredPane2.setLayer(panelAdmin, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(panelUsuario, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane2Layout = new javax.swing.GroupLayout(jLayeredPane2);
        jLayeredPane2.setLayout(jLayeredPane2Layout);
        jLayeredPane2Layout.setHorizontalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 682, Short.MAX_VALUE)
            .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jLayeredPane2Layout.setVerticalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 444, Short.MAX_VALUE)
            .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        archivoMenu.setText("Archivo");

        logout.setText("Cerrar sesión");
        logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutActionPerformed(evt);
            }
        });
        archivoMenu.add(logout);

        salirMenuItem.setText("Salir");
        salirMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salirMenuItemActionPerformed(evt);
            }
        });
        archivoMenu.add(salirMenuItem);

        adminMenuBar.add(archivoMenu);

        mainAplicacion.setJMenuBar(adminMenuBar);

        javax.swing.GroupLayout mainAplicacionLayout = new javax.swing.GroupLayout(mainAplicacion.getContentPane());
        mainAplicacion.getContentPane().setLayout(mainAplicacionLayout);
        mainAplicacionLayout.setHorizontalGroup(
            mainAplicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane2)
        );
        mainAplicacionLayout.setVerticalGroup(
            mainAplicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        peticionIP.setModal(true);
        peticionIP.setResizable(false);
        peticionIP.setSize(new java.awt.Dimension(385, 177));

        jLabel3.setText("Introduzca IP del servidor");

        txtIP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtIPKeyPressed(evt);
            }
        });

        btnConectarBD.setText("Conectar");
        btnConectarBD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConectarBDActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout peticionIPLayout = new javax.swing.GroupLayout(peticionIP.getContentPane());
        peticionIP.getContentPane().setLayout(peticionIPLayout);
        peticionIPLayout.setHorizontalGroup(
            peticionIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peticionIPLayout.createSequentialGroup()
                .addGroup(peticionIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(peticionIPLayout.createSequentialGroup()
                        .addGap(106, 106, 106)
                        .addGroup(peticionIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtIP)))
                    .addGroup(peticionIPLayout.createSequentialGroup()
                        .addGap(142, 142, 142)
                        .addComponent(btnConectarBD)))
                .addContainerGap(106, Short.MAX_VALUE))
        );
        peticionIPLayout.setVerticalGroup(
            peticionIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peticionIPLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnConectarBD)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        nuevoUsuario.setTitle("Nuevo usuario");
        nuevoUsuario.setModal(true);
        nuevoUsuario.setResizable(false);
        nuevoUsuario.setSize(new java.awt.Dimension(271, 262));

        jLabel4.setText("Usuario");

        jLabel5.setText("Contraseña");

        txtNuevaClave.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNuevaClaveKeyTyped(evt);
            }
        });

        jLabel6.setText("Fortaleza de la contraseña");

        btnCancelarNuevoUsuario.setText("Cancelar");
        btnCancelarNuevoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarNuevoUsuarioActionPerformed(evt);
            }
        });

        btnAceptarNuevoUsuario.setText("Aceptar");
        btnAceptarNuevoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarNuevoUsuarioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout nuevoUsuarioLayout = new javax.swing.GroupLayout(nuevoUsuario.getContentPane());
        nuevoUsuario.getContentPane().setLayout(nuevoUsuarioLayout);
        nuevoUsuarioLayout.setHorizontalGroup(
            nuevoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nuevoUsuarioLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(nuevoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(txtNuevoUsuario)
                    .addComponent(txtNuevaClave)
                    .addComponent(lblFortaleza, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(44, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nuevoUsuarioLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAceptarNuevoUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancelarNuevoUsuario)
                .addContainerGap())
        );
        nuevoUsuarioLayout.setVerticalGroup(
            nuevoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nuevoUsuarioLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNuevoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNuevaClave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFortaleza, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(nuevoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelarNuevoUsuario)
                    .addComponent(btnAceptarNuevoUsuario))
                .addContainerGap())
        );

        editarUsuario.setTitle("Editar usuario");
        editarUsuario.setModal(true);
        editarUsuario.setResizable(false);
        editarUsuario.setSize(new java.awt.Dimension(271, 238));

        jLabel8.setText("Nueva contraseña");

        txtEditarClave.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtEditarClaveKeyTyped(evt);
            }
        });

        jLabel9.setText("Fortaleza de la contraseña");

        btnCancelarEditarUsuario.setText("Cancelar");
        btnCancelarEditarUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarEditarUsuarioActionPerformed(evt);
            }
        });

        btnAceptarEditarUsuario.setText("Aceptar");
        btnAceptarEditarUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarEditarUsuarioActionPerformed(evt);
            }
        });

        jLabel10.setText("Usuario");

        jLabel11.setText("Contraseña");

        javax.swing.GroupLayout editarUsuarioLayout = new javax.swing.GroupLayout(editarUsuario.getContentPane());
        editarUsuario.getContentPane().setLayout(editarUsuarioLayout);
        editarUsuarioLayout.setHorizontalGroup(
            editarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editarUsuarioLayout.createSequentialGroup()
                .addGroup(editarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editarUsuarioLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAceptarEditarUsuario)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelarEditarUsuario))
                    .addGroup(editarUsuarioLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(editarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(txtEditarClave, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(lblFortaleza2, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editarUsuarioLayout.createSequentialGroup()
                        .addGap(0, 22, Short.MAX_VALUE)
                        .addGroup(editarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(editarUsuarioLayout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(70, 70, 70)
                                .addComponent(jLabel11))
                            .addGroup(editarUsuarioLayout.createSequentialGroup()
                                .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblClave, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        editarUsuarioLayout.setVerticalGroup(
            editarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editarUsuarioLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(editarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(6, 6, 6)
                .addGroup(editarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblClave, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addGap(2, 2, 2)
                .addComponent(txtEditarClave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addComponent(jLabel9)
                .addGap(2, 2, 2)
                .addComponent(lblFortaleza2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(editarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAceptarEditarUsuario)
                    .addComponent(btnCancelarEditarUsuario))
                .addContainerGap())
        );

        nuevaNoticia.setTitle("Nueva noticia");
        nuevaNoticia.setModal(true);
        nuevaNoticia.setResizable(false);
        nuevaNoticia.setSize(new java.awt.Dimension(503, 376));

        jLabel12.setText("Fecha: ");

        jLabel13.setText("Departamento: ");

        btnImagen.setText("Seleccionar imagen");
        btnImagen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImagenActionPerformed(evt);
            }
        });

        jLabel14.setText("Texto");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane4.setViewportView(jTextArea1);

        jLabel15.setText("Vigencia: ");

        jLabel16.setText("Posición del texto");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonGroup1.add(topLeft);
        topLeft.setActionCommand("topLeft");
        topLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topLeftActionPerformed(evt);
            }
        });

        buttonGroup1.add(topRight);
        topRight.setActionCommand("topRight");
        topRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topRightActionPerformed(evt);
            }
        });

        buttonGroup1.add(bottomLeft);
        bottomLeft.setActionCommand("bottomLeft");
        bottomLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomLeftActionPerformed(evt);
            }
        });

        buttonGroup1.add(bottomRight);
        bottomRight.setActionCommand("bottomRight");
        bottomRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomRightActionPerformed(evt);
            }
        });

        buttonGroup1.add(middleLeft);
        middleLeft.setActionCommand("middleLeft");
        middleLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                middleLeftActionPerformed(evt);
            }
        });

        buttonGroup1.add(middleRight);
        middleRight.setActionCommand("middleRight");
        middleRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                middleRightActionPerformed(evt);
            }
        });

        buttonGroup1.add(bottomCenter);
        bottomCenter.setActionCommand("bottomCenter");
        bottomCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomCenterActionPerformed(evt);
            }
        });

        buttonGroup1.add(topCenter);
        topCenter.setActionCommand("topCenter");
        topCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topCenterActionPerformed(evt);
            }
        });

        buttonGroup1.add(middleCenter);
        middleCenter.setActionCommand("middleCenter");
        middleCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                middleCenterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bottomLeft)
                    .addComponent(topLeft)
                    .addComponent(middleLeft))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(middleCenter)
                        .addGap(76, 76, 76)
                        .addComponent(middleRight))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(topCenter)
                        .addGap(81, 81, 81)
                        .addComponent(topRight))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(bottomCenter)
                        .addGap(76, 76, 76)
                        .addComponent(bottomRight)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(topCenter)
                    .addComponent(topRight)
                    .addComponent(topLeft))
                .addGap(51, 51, 51)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(middleCenter)
                    .addComponent(middleLeft)
                    .addComponent(middleRight))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bottomLeft, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bottomRight, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bottomCenter, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        btnVistaPrevia.setText("Vista previa");
        btnVistaPrevia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVistaPreviaActionPerformed(evt);
            }
        });

        jLabel17.setText("días");

        btnCancelarNuevaNoticia.setText("Cancelar");

        btnAceptarNuevaNoticia.setText("Aceptar");
        btnAceptarNuevaNoticia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarNuevaNoticiaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout nuevaNoticiaLayout = new javax.swing.GroupLayout(nuevaNoticia.getContentPane());
        nuevaNoticia.getContentPane().setLayout(nuevaNoticiaLayout);
        nuevaNoticiaLayout.setHorizontalGroup(
            nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImagen)
                    .addComponent(jLabel14)
                    .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtVigencia, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(jLabel17)))
                .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDepartamento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 33, Short.MAX_VALUE))
                            .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                                .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                                        .addComponent(btnVistaPrevia)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nuevaNoticiaLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(btnAceptarNuevaNoticia)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCancelarNuevaNoticia)))))
                .addContainerGap())
        );
        nuevaNoticiaLayout.setVerticalGroup(
            nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12)
                        .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel13)
                        .addComponent(lblDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(31, 31, 31)
                .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                        .addComponent(btnImagen)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nuevaNoticiaLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(txtVigencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnVistaPrevia)
                            .addComponent(jLabel17))
                        .addContainerGap(52, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nuevaNoticiaLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(nuevaNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCancelarNuevaNoticia)
                            .addComponent(btnAceptarNuevaNoticia))
                        .addContainerGap())))
        );

        vistaPrevia.setTitle("Vista previa");
        vistaPrevia.setModal(true);
        vistaPrevia.setResizable(false);
        vistaPrevia.setSize(new java.awt.Dimension(600, 400));

        javax.swing.GroupLayout panelVistaPreviaLayout = new javax.swing.GroupLayout(panelVistaPrevia);
        panelVistaPrevia.setLayout(panelVistaPreviaLayout);
        panelVistaPreviaLayout.setHorizontalGroup(
            panelVistaPreviaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        panelVistaPreviaLayout.setVerticalGroup(
            panelVistaPreviaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout vistaPreviaLayout = new javax.swing.GroupLayout(vistaPrevia.getContentPane());
        vistaPrevia.getContentPane().setLayout(vistaPreviaLayout);
        vistaPreviaLayout.setHorizontalGroup(
            vistaPreviaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelVistaPrevia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        vistaPreviaLayout.setVerticalGroup(
            vistaPreviaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelVistaPrevia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        editarNoticia.setTitle("Ver noticia");
        editarNoticia.setModal(true);
        editarNoticia.setResizable(false);
        editarNoticia.setSize(new java.awt.Dimension(519, 276));

        jLabel7.setText("Id Noticia");

        jLabel18.setText("Departamento");

        jLabel19.setText("Fecha");

        jLabel20.setText("Ruta imagen");

        jLabel21.setText("Días de vigencia");

        chkVigente.setText("Vigente");

        chkPublica.setText("Pública");

        jButton1.setText("Ver imagen");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Cancelar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Aceptar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout editarNoticiaLayout = new javax.swing.GroupLayout(editarNoticia.getContentPane());
        editarNoticia.getContentPane().setLayout(editarNoticiaLayout);
        editarNoticiaLayout.setHorizontalGroup(
            editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editarNoticiaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtFecha, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtIdNot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(txtDiasVigencia, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkVigente)
                    .addComponent(chkPublica)
                    .addComponent(jLabel21))
                .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editarNoticiaLayout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(editarNoticiaLayout.createSequentialGroup()
                                .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(editarNoticiaLayout.createSequentialGroup()
                                        .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel20)
                                            .addComponent(jLabel18))
                                        .addGap(0, 192, Short.MAX_VALUE))
                                    .addComponent(txtRutaImagen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(12, 12, 12))
                            .addGroup(editarNoticiaLayout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editarNoticiaLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addContainerGap())))
        );
        editarNoticiaLayout.setVerticalGroup(
            editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editarNoticiaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtIdNot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRutaImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editarNoticiaLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(editarNoticiaLayout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDiasVigencia, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton1))
                        .addGap(18, 18, 18)
                        .addComponent(chkVigente)
                        .addGap(24, 24, 24)
                        .addComponent(chkPublica)
                        .addContainerGap(39, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editarNoticiaLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(editarNoticiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jButton3))
                        .addContainerGap())))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Proyecto RASPanel");
        setResizable(false);
        setSize(new java.awt.Dimension(401, 262));

        jLabel1.setText("USUARIO");

        jLabel2.setText("CONTRASEÑA");

        btnLogin.setText("ENTRAR");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtPasswordKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(txtUser)
                            .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(btnLogin)))
                .addContainerGap(108, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnLogin)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        //System.out.println("Usuario: "+txtUser.getText());
        //System.out.println("Clave: "+String.valueOf(txtPassword.getPassword()));
        if (txtUser.getText().trim().equals("") || String.valueOf(txtPassword.getPassword()).trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Usuario o contraseña en blanco");
        }else {
            if (!new File("administrador.dat").exists()) {
                if (txtUser.getText().equals("admin") && String.valueOf(txtPassword.getPassword()).equals("admin")) {
                    peticionIP.setLocationRelativeTo(null);
                    peticionIP.setVisible(true);
                }
            }else if (new File("administrador.dat").exists() && admin()) {
                peticionIP.setLocationRelativeTo(null);
                peticionIP.setVisible(true);
            }else if (new File("usuarios.dat").exists() && usuario()){
                try {
                    BufferedReader br = new BufferedReader(new FileReader("ipservidor.txt"));
                    String linea;
                    
                    while ((linea=br.readLine())!=null) {
                        con = new Conector(linea);
                    }
                    br.close();
                    
                    listarNoticiasUser();
                    panelAdmin.setVisible(false);
                    panelUsuario.setVisible(true);
                    mainAplicacion.setLocationRelativeTo(null);
                    mainAplicacion.pack();
                    mainAplicacion.setTitle(txtUser.getText());
                    mainAplicacion.setVisible(true);
                    this.setVisible(false);
                } catch (IOException e) {
                    System.out.println("Error accediendo al fichero ipservidor.txt");
                    e.printStackTrace();
                }
            }else {
                JOptionPane.showMessageDialog(null, "El usuario no existe");
            }
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnUsuariosAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuariosAdminActionPerformed
        tablaUsuarios.clearSelection();
        panelUsuarios.setVisible(true);
        panelNoticias.setVisible(false);
    }//GEN-LAST:event_btnUsuariosAdminActionPerformed

    private void btnNoticiasAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNoticiasAdminActionPerformed
        tablaTodasNoticias.clearSelection();
        panelUsuarios.setVisible(false);
        panelNoticias.setVisible(true);
        btnModUsuario.setEnabled(false);
        btnEliminarUsuario.setEnabled(false);
    }//GEN-LAST:event_btnNoticiasAdminActionPerformed

    private void salirMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salirMenuItemActionPerformed
        con.cerrar();
        System.exit(0);
    }//GEN-LAST:event_salirMenuItemActionPerformed

    private void btnConectarBDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConectarBDActionPerformed
        if (txtIP.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe introducir una IP");
        }else {
            String ip = txtIP.getText();
            con = new Conector(ip);
            if (con.getConexion()!=null) {
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(new File("ipservidor.txt")));
                    bw.write(ip);
                    bw.close();
                } catch (IOException e) {
                    System.out.println("Error creando el fichero ipservidor.txt");
                    e.printStackTrace();
                }
                listarUsuarios();
                listarNoticiasAdmin();
                panelAdmin.setVisible(true);
                panelUsuario.setVisible(false);
                panelUsuarios.setVisible(true);
                panelNoticias.setVisible(false);
                mainAplicacion.setLocationRelativeTo(null);
                mainAplicacion.pack();
                mainAplicacion.setTitle("Administrador");
                mainAplicacion.setVisible(true);
                peticionIP.setVisible(false);
                this.setVisible(false);
            }
        }
    }//GEN-LAST:event_btnConectarBDActionPerformed

    private void mainAplicacionWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_mainAplicacionWindowClosing
        con.cerrar();
    }//GEN-LAST:event_mainAplicacionWindowClosing

    private void txtPasswordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPasswordKeyPressed
        //10 = Intro
        if (evt.getKeyCode()==10) {
            btnLogin.doClick();
        }
    }//GEN-LAST:event_txtPasswordKeyPressed

    private void txtIPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtIPKeyPressed
        if (evt.getKeyCode()==10) {
            btnConectarBD.doClick();
        }
    }//GEN-LAST:event_txtIPKeyPressed

    private void txtNuevaClaveKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNuevaClaveKeyTyped
        if (calculatePasswordStrength(txtNuevaClave.getText())<5) {
            lblFortaleza.setText("Débil");
            lblFortaleza.setForeground(Color.red);
        } else if (calculatePasswordStrength(txtNuevaClave.getText())>=5 && calculatePasswordStrength(txtNuevaClave.getText())<8) {
            lblFortaleza.setText("Media");
            lblFortaleza.setForeground(Color.yellow);
        } else {
            lblFortaleza.setText("Fuerte");
            lblFortaleza.setForeground(Color.green);
        }
    }//GEN-LAST:event_txtNuevaClaveKeyTyped

    private void btnNuevoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoUsuarioActionPerformed
        txtNuevoUsuario.setText("");
        txtNuevaClave.setText("");
        lblFortaleza.setText("");
        txtNuevoUsuario.requestFocus();
        nuevoUsuario.setLocationRelativeTo(null);
        nuevoUsuario.setVisible(true);
    }//GEN-LAST:event_btnNuevoUsuarioActionPerformed

    private void btnCancelarNuevoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarNuevoUsuarioActionPerformed
        nuevoUsuario.setVisible(false);
    }//GEN-LAST:event_btnCancelarNuevoUsuarioActionPerformed

    private void btnAceptarNuevoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarNuevoUsuarioActionPerformed
        if (!txtNuevoUsuario.getText().equals("") && !txtNuevaClave.getText().equals("")) {
            Departamento d = new Departamento(txtNuevoUsuario.getText(), txtNuevaClave.getText());
            con.addDepartamento(d);
            nuevoUsuario.setVisible(false);
            listarUsuarios();
            JOptionPane.showMessageDialog(null, "Nuevo usuario creado correctamente");
        }else {
            JOptionPane.showMessageDialog(null, "Algún campo vacío");
        }
    }//GEN-LAST:event_btnAceptarNuevoUsuarioActionPerformed

    private void btnEliminarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarUsuarioActionPerformed
        // 0-Sí 1-No
        if (JOptionPane.showConfirmDialog(null, "¿Desea eliminar el usuario?", "Eliminar usuario", JOptionPane.YES_NO_OPTION)==0) {
            String departamento = (String) tablaUsuarios.getValueAt(tablaUsuarios.getSelectedRow(), 0);
            con.eliminarDepartamento(departamento);
            listarUsuarios();
            listarNoticiasAdmin();
            JOptionPane.showMessageDialog(null, "Usuario eliminado correctamente");
            btnModUsuario.setEnabled(false);
            btnEliminarUsuario.setEnabled(false);
        }
    }//GEN-LAST:event_btnEliminarUsuarioActionPerformed

    private void tablaUsuariosMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaUsuariosMouseReleased
        if (tablaUsuarios.getSelectedRow()>-1) {
            btnModUsuario.setEnabled(true);
            btnEliminarUsuario.setEnabled(true);
        }
    }//GEN-LAST:event_tablaUsuariosMouseReleased

    private void btnModUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModUsuarioActionPerformed
        lblUsuario.setText((String) tablaUsuarios.getValueAt(tablaUsuarios.getSelectedRow(), 0));
        lblClave.setText((String) tablaUsuarios.getValueAt(tablaUsuarios.getSelectedRow(), 1));
        txtEditarClave.setText("");
        lblFortaleza2.setText("");
        editarUsuario.setLocationRelativeTo(null);
        editarUsuario.pack();
        editarUsuario.setVisible(true);
    }//GEN-LAST:event_btnModUsuarioActionPerformed

    private void txtEditarClaveKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEditarClaveKeyTyped
        if (calculatePasswordStrength(txtEditarClave.getText())<5) {
            lblFortaleza2.setText("Débil");
            lblFortaleza2.setForeground(Color.red);
        } else if (calculatePasswordStrength(txtEditarClave.getText())>=5 && calculatePasswordStrength(txtEditarClave.getText())<8) {
            lblFortaleza2.setText("Media");
            lblFortaleza2.setForeground(Color.yellow);
        } else {
            lblFortaleza2.setText("Fuerte");
            lblFortaleza2.setForeground(Color.green);
        }
    }//GEN-LAST:event_txtEditarClaveKeyTyped

    private void btnCancelarEditarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarEditarUsuarioActionPerformed
        editarUsuario.setVisible(false);
    }//GEN-LAST:event_btnCancelarEditarUsuarioActionPerformed

    private void btnAceptarEditarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarEditarUsuarioActionPerformed
        if (txtEditarClave.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Algún campo vacío");
        }else {
            String usuario = lblUsuario.getText();
            String clave = txtEditarClave.getText();
            Departamento d = new Departamento(usuario, clave);
            con.modDepartamento(d);
            editarUsuario.setVisible(false);
            listarUsuarios();
            listarNoticiasAdmin();
            JOptionPane.showMessageDialog(null, "Usuario modificado correctamente");
            btnModUsuario.setEnabled(false);
            btnEliminarUsuario.setEnabled(false);
        }
    }//GEN-LAST:event_btnAceptarEditarUsuarioActionPerformed

    private void btnNuevaNoticiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaNoticiaActionPerformed
        lblFecha.setText(df.format(date));
        lblDepartamento.setText(mainAplicacion.getTitle());
        jTextArea1.setText("");
        txtVigencia.setText("");
        buttonGroup1.clearSelection();
        nuevaNoticia.setLocationRelativeTo(null);
        nuevaNoticia.pack();
        nuevaNoticia.setVisible(true);
    }//GEN-LAST:event_btnNuevaNoticiaActionPerformed

    private void btnImagenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImagenActionPerformed
        //Se crea un filtro para los ficheros a leer
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Ficheros de imagen", "jpg", "png");
        jFileChooser1.setFileFilter(filter);

        //Valor que retorna al elegir una opcion en el file chooser
        int opcion = this.jFileChooser1.showOpenDialog(this);
        if (opcion == JFileChooser.APPROVE_OPTION) {
            //El path absoluto del archivo elegido
            imagen_seleccionada = this.jFileChooser1.getSelectedFile();
            System.out.println(imagen_seleccionada);
        }
    }//GEN-LAST:event_btnImagenActionPerformed

    private void btnVistaPreviaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVistaPreviaActionPerformed
        if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
        }else if (jTextArea1.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
        }else if (!positionSelected()) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una posición");
        }else {
            try {
                panelVistaPrevia.removeAll();
                BufferedImage brVistaPrevia = ImageIO.read(new File("vistaprevia"));
                JLabel labelImg = new JLabel(new ImageIcon(brVistaPrevia));
                //Crea un panel donde poner la imagen
                JPanel panelImagen = new JPanel();
                //Se establece posición y tamaño
                panelImagen.setBounds(0, 0, 600, 400);
                panelImagen.add(labelImg);//Se añade la imagen al Panel
                panelVistaPrevia.add(panelImagen);//Se añade el Panel de la Imagen
                vistaPrevia.setLocationRelativeTo(null);
                vistaPrevia.setVisible(true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnVistaPreviaActionPerformed

    private void btnAceptarNuevaNoticiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarNuevaNoticiaActionPerformed
        if (image==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
        } if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
        } if (txtVigencia.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe especificar los días de vigencia");
        } else {
            try {
                boolean insertar = false;
                
                int idNoticia = con.maxIdNot();// ID de la noticia
                String departamento = lblDepartamento.getText();// Departamento al que pertenece la noticia
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedImage smallImage = resize(image, 600, 400);
                ImageIO.write(smallImage, "png", baos);
                this.imageblob = baos.toByteArray();// Imagen convertida a bytes para guardarla en la base de datos
                
                String fecha = lblFecha.getText();// Fecha de la noticia
                
                BufferedImage largeImage = resize(image, 1200, 768);
                File imagen = new File(lblDepartamento.getText()+"_"+con.maxIdNot()+"_"+lblFecha.getText());
                String ruta = imagen.getName();// Ruta de la imagen
                //String ruta = "/home/pi/Pictures/" + imagen.getName();
                //HE eliminado esto porque ha de guardarlo de forma temporal en la carpeta actual y luego borrar el fichero
                System.out.println(ruta);
                ImageIO.write(largeImage, "png", new File(ruta));// Se guarda la imagen reescalada para mostrarla en la televisión, en la ruta especificada
                imagen.delete();//Importante, borrar la imagen para que no quede en el pc local
                int diasVigencia = 0;// Días de vigencia
                
                try {
                    diasVigencia = Integer.parseInt(txtVigencia.getText());
                    insertar = true;
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Días de vigencia incorrectos");
                }
                
                int vigente = 0;// Booleano de si es vigente o no | 0->false 1->true
                int publica = 0;// Booleano de si es pública o no | 0->false 1->true
                
                if (insertar) {
                    con.addNoticia(new Noticia(idNoticia, diasVigencia, vigente, publica, departamento, fecha, ruta, imageblob));
                    imagen_seleccionada = null;
                    image = null;
                    imageblob = null;
                    nuevaNoticia.setVisible(false);
                    btnVerNoticia.setEnabled(false);
                    listarNoticiasUser();
                    JOptionPane.showMessageDialog(null, "Noticia creada correctamente");
                }
                
                //ENVÍA IMAGEN
                try {
                  this.envia_imagen_sftp_y_reinicio(ruta);
                } catch (JSchException ex) {
                    Logger.getLogger(Aplicacion.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SftpException ex) {
                    Logger.getLogger(Aplicacion.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnAceptarNuevaNoticiaActionPerformed

    private void envia_imagen_sftp_y_reinicio(String ruta) throws JSchException, SftpException{

            String user = "pi";
            String host = "192.168.0.157";
            Integer port = 22;
            String pass = "pi2018..";
            
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            UserInfo ui;
            ui = new SUserInfo(pass, null);
            
            session.setUserInfo((UserInfo) ui);
            session.setPassword(pass);
            session.connect();
            
            ChannelSftp sftp = (ChannelSftp)session.openChannel("sftp");
            sftp.connect();
            
            sftp.cd("/home/pi/RasPanel/Pictures/");
            System.out.println("Subiendo "+ruta);
            sftp.put(ruta, ruta + ".png");
            
            System.out.println("Archivos subidos.");
            
            sftp.exit();
            sftp.disconnect();
            //session.disconnect();
            
            //Ahora llama por SSH al script que comprueba la vigencia
         /*   JSch jsch2 = new JSch();
            Session session2 = jsch.getSession(user, host, port);
            UserInfo ui2 = new SUserInfo(pass, null);
            session2.setUserInfo(ui);
            session2.setPassword(pass);*/
            
            
          /*  int paso=0;
             try {
             session2.connect();
             System.out.println("Conexión realizada");//Si llega aquí es que todo está correcto
             paso=1;
            }catch (JSchException e) {
                System.out.println("ERROR: "+e.getMessage());
            }*/
        
           // if (paso==1){//Si no hay errores  
            ChannelExec channelExec;                             
                channelExec = (ChannelExec)session.openChannel("exec");

            System.out.println("ENVIARDO COMANDO SSH");
            channelExec.setCommand("/home/pi/RasPanel/comprueba_vigencia.sh");
            channelExec.connect();

            channelExec.disconnect();
            session.disconnect();

            System.out.println("------ FIN");
        
            
            

    
    
    
    }
            
    private void jTable1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseReleased
        if (jTable1.getSelectedRow()>-1) {
            btnVerNoticia.setEnabled(true);
        }
    }//GEN-LAST:event_jTable1MouseReleased

    private void topLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topLeftActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 100, 100);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_topLeftActionPerformed

    private void topCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topCenterActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 600, 100);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_topCenterActionPerformed

    private void topRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topRightActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 900, 100);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_topRightActionPerformed

    private void middleLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_middleLeftActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 100, 500);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_middleLeftActionPerformed

    private void middleCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_middleCenterActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 600, 500);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_middleCenterActionPerformed

    private void middleRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_middleRightActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 900, 500);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_middleRightActionPerformed

    private void bottomLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomLeftActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 100, 800);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_bottomLeftActionPerformed

    private void bottomCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomCenterActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 600, 800);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_bottomCenterActionPerformed

    private void bottomRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomRightActionPerformed
        if (jTextArea1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Debe escribir algo");
            buttonGroup1.clearSelection();
        } else if (imagen_seleccionada==null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen");
            buttonGroup1.clearSelection();
        } else {
            try {
                System.out.println("Posición: "+evt.getActionCommand());
                String texto = jTextArea1.getText();
                image = ImageIO.read(imagen_seleccionada);// Se toma la imagen seleccionada
                System.out.println("Imagen cargada");
                BufferedImage imagenVistaPrevia = null;
                imagenVistaPrevia = writeImage(image, texto, 900, 800);
                ImageIO.write(imagenVistaPrevia, "png", new File("vistaprevia"));
                System.out.println("Vista previa creada");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_bottomRightActionPerformed

    private void tablaTodasNoticiasMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaTodasNoticiasMouseReleased
        if (tablaTodasNoticias.getSelectedRow()>-1) {
            btnEliminarNoticia.setEnabled(true);
            btnEditarNoticia.setEnabled(true);
        }
    }//GEN-LAST:event_tablaTodasNoticiasMouseReleased

    private void btnEditarNoticiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarNoticiaActionPerformed
        int idNot = (int) tablaTodasNoticias.getValueAt(tablaTodasNoticias.getSelectedRow(), 0);
        System.out.println("Id seleccionado: "+idNot);
        Noticia noticia = con.getNoticia(idNot);
        txtIdNot.setText(Integer.toString(noticia.getIdNoticia()));
        txtDepartamento.setText(noticia.getDepartamento());
        txtFecha.setText(noticia.getFecha());
        txtRutaImagen.setText(noticia.getRuta());
        txtDiasVigencia.setText(Integer.toString(noticia.getDiasVigencia()));
        
        if (noticia.getVigente()==0)
            chkVigente.setSelected(false);
        else
            chkVigente.setSelected(true);
        
        if (noticia.getPublica()==0)
            chkPublica.setSelected(false);
        else
            chkPublica.setSelected(true);
        
        byte[] imagen = noticia.getImagen();
        
        try {
            panelVistaPrevia.removeAll();
            BufferedImage brImagen = ImageIO.read(new ByteArrayInputStream(imagen));
            JLabel labelImg = new JLabel(new ImageIcon(brImagen));
            //Crea un panel donde poner la imagen
            JPanel panelImagen = new JPanel();
            //Se establece posición y tamaño
            panelImagen.setBounds(0, 0, 600, 400);
            panelImagen.add(labelImg);//Se añade la imagen al Panel
            panelVistaPrevia.add(panelImagen);//Se añade el Panel de la Imagen
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        jButton3.setVisible(true);
        jButton2.setText("Cancelar");
        chkVigente.setEnabled(true);
        chkPublica.setEnabled(true);
        editarNoticia.setLocationRelativeTo(null);
        editarNoticia.setVisible(true);
    }//GEN-LAST:event_btnEditarNoticiaActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        vistaPrevia.setLocationRelativeTo(null);
        vistaPrevia.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        editarNoticia.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
        Connection c = con.getConexion();
        if (c!=null) {
            mainAplicacion.setVisible(false);
            con.cerrar();
            txtUser.setText("");
            txtPassword.setText("");
            txtUser.requestFocus();
            this.setVisible(true);
        }
    }//GEN-LAST:event_logoutActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int idNot = Integer.parseInt(txtIdNot.getText());
        int vigente = chkVigente.isSelected()? 1:0;
        int publica = chkPublica.isSelected()? 1:0;
        
        con.modNoticia(new Noticia(idNot, vigente, publica));
        JOptionPane.showMessageDialog(null, "Noticia modificada correctamente");
        listarNoticiasAdmin();
        btnEditarNoticia.setEnabled(false);
        btnEliminarNoticia.setEnabled(false);
        editarNoticia.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnVerNoticiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerNoticiaActionPerformed
        int idNot = (int) jTable1.getValueAt(jTable1.getSelectedRow(), 0);
        System.out.println("Id seleccionado: "+idNot);
        Noticia noticia = con.getNoticia(idNot);
        txtIdNot.setText(Integer.toString(noticia.getIdNoticia()));
        txtDepartamento.setText(noticia.getDepartamento());
        txtFecha.setText(noticia.getFecha());
        txtRutaImagen.setText(noticia.getRuta());
        txtDiasVigencia.setText(Integer.toString(noticia.getDiasVigencia()));
        
        if (noticia.getVigente()==0)
            chkVigente.setSelected(false);
        else
            chkVigente.setSelected(true);
        
        if (noticia.getPublica()==0)
            chkPublica.setSelected(false);
        else
            chkPublica.setSelected(true);
        
        byte[] imagen = noticia.getImagen();
        
        try {
            panelVistaPrevia.removeAll();
            BufferedImage brImagen = ImageIO.read(new ByteArrayInputStream(imagen));
            JLabel labelImg = new JLabel(new ImageIcon(brImagen));
            //Crea un panel donde poner la imagen
            JPanel panelImagen = new JPanel();
            //Se establece posición y tamaño
            panelImagen.setBounds(0, 0, 600, 400);
            panelImagen.add(labelImg);//Se añade la imagen al Panel
            panelVistaPrevia.add(panelImagen);//Se añade el Panel de la Imagen
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        jButton3.setVisible(false);
        jButton2.setText("Aceptar");
        chkVigente.setEnabled(false);
        chkPublica.setEnabled(false);
        editarNoticia.setLocationRelativeTo(null);
        editarNoticia.setVisible(true);
    }//GEN-LAST:event_btnVerNoticiaActionPerformed

    private void btnEliminarNoticiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarNoticiaActionPerformed
        int idNot = (int) tablaTodasNoticias.getValueAt(tablaTodasNoticias.getSelectedRow(), 0);
        con.delNoticia(idNot);
        JOptionPane.showMessageDialog(null, "Noticia eliminada correctamente");
        listarNoticiasAdmin();
    }//GEN-LAST:event_btnEliminarNoticiaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Aplicacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Aplicacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Aplicacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Aplicacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Aplicacion aplicacion = new Aplicacion();
                aplicacion.setLocationRelativeTo(null);
                aplicacion.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar adminMenuBar;
    private javax.swing.JMenu archivoMenu;
    private javax.swing.JRadioButton bottomCenter;
    private javax.swing.JRadioButton bottomLeft;
    private javax.swing.JRadioButton bottomRight;
    private javax.swing.JButton btnAceptarEditarUsuario;
    private javax.swing.JButton btnAceptarNuevaNoticia;
    private javax.swing.JButton btnAceptarNuevoUsuario;
    private javax.swing.JButton btnCancelarEditarUsuario;
    private javax.swing.JButton btnCancelarNuevaNoticia;
    private javax.swing.JButton btnCancelarNuevoUsuario;
    private javax.swing.JButton btnConectarBD;
    private javax.swing.JButton btnEditarNoticia;
    private javax.swing.JButton btnEliminarNoticia;
    private javax.swing.JButton btnEliminarUsuario;
    private javax.swing.JButton btnImagen;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnModUsuario;
    private javax.swing.JButton btnNoticiasAdmin;
    private javax.swing.JButton btnNuevaNoticia;
    private javax.swing.JButton btnNuevoUsuario;
    private javax.swing.JButton btnUsuariosAdmin;
    private javax.swing.JButton btnVerNoticia;
    private javax.swing.JButton btnVistaPrevia;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkPublica;
    private javax.swing.JCheckBox chkVigente;
    private javax.swing.JDialog editarNoticia;
    private javax.swing.JDialog editarUsuario;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblClave;
    private javax.swing.JLabel lblDepartamento;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFortaleza;
    private javax.swing.JLabel lblFortaleza2;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JMenuItem logout;
    private javax.swing.JFrame mainAplicacion;
    private javax.swing.JRadioButton middleCenter;
    private javax.swing.JRadioButton middleLeft;
    private javax.swing.JRadioButton middleRight;
    private javax.swing.JDialog nuevaNoticia;
    private javax.swing.JDialog nuevoUsuario;
    private javax.swing.JPanel panelAdmin;
    private javax.swing.JPanel panelNoticias;
    private javax.swing.JPanel panelUsuario;
    private javax.swing.JPanel panelUsuarios;
    private javax.swing.JPanel panelVistaPrevia;
    private javax.swing.JDialog peticionIP;
    private javax.swing.JMenuItem salirMenuItem;
    private javax.swing.JTable tablaTodasNoticias;
    private javax.swing.JTable tablaUsuarios;
    private javax.swing.JRadioButton topCenter;
    private javax.swing.JRadioButton topLeft;
    private javax.swing.JRadioButton topRight;
    private javax.swing.JLabel txtDepartamento;
    private javax.swing.JLabel txtDiasVigencia;
    private javax.swing.JTextField txtEditarClave;
    private javax.swing.JLabel txtFecha;
    private javax.swing.JTextField txtIP;
    private javax.swing.JLabel txtIdNot;
    private javax.swing.JTextField txtNuevaClave;
    private javax.swing.JTextField txtNuevoUsuario;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JLabel txtRutaImagen;
    private javax.swing.JTextField txtUser;
    private javax.swing.JTextField txtVigencia;
    private javax.swing.JDialog vistaPrevia;
    // End of variables declaration//GEN-END:variables
}
