/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectofinal;

import com.mysql.jdbc.Connection;
import java.awt.HeadlessException;
import java.io.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author juancarlos
 */
public class Conector {
    private Connection conexion = null;
    private String sql;
    private Statement stmnt;
    private PreparedStatement pstmnt;
    private final String USER = "root";
    private final String PASSWORD = "pi2018..";

    public Conector(String ip) {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            this.conexion = (Connection) DriverManager.getConnection("jdbc:mysql://"+ip+"/noticias", USER, PASSWORD);
            sql = "SELECT * FROM sqlite_master WHERE name='Departamento'";// Comprueba si existe la tabla
            stmnt = conexion.createStatement();
            ResultSet rs = stmnt.executeQuery(sql);
            if (rs.next()==false) {// Si no existe crea la base de datos
                this.crearTablas();
            }
            JOptionPane.showMessageDialog(null, "Base de datos conectada");
            System.out.println("Base de datos conectada.");
        } catch (ClassNotFoundException | SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error conectando con la base de datos");
            System.out.println("Error cargando el driver");
            //ex.printStackTrace();
        }
    }
    
    public void crearTablas() {
        try {
            stmnt = conexion.createStatement();
            
            sql = "CREATE TABLE IF NOT EXISTS Departamento ("
                    + "Departamento  VARCHAR(30),"
                    + "Clave    VARCHAR(30),"
                    + "PRIMARY KEY(Departamento))";
            stmnt.execute(sql);
            
            sql = "CREATE TABLE IF NOT EXISTS Noticia("
                    + "IdNot    INTEGER AUTO_INCREMENT,"
                    + "Departamento   VARCHAR(30) REFERENCES Departamento(Departamento)"
                        + "ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "Imagen   LONGBLOB,"
                    + "Fecha    DATE,"// Formato: YYYY-MM-DD
                    + "Ruta     VARCHAR(80),"// Ruta donde se guardará la foto en el equipo
                    + "DiasVigencia INTEGER,"
                    + "Vigente  BOOLEAN,"
                    + "Publica  BOOLEAN,"// Campo del visto bueno (checkbox)
                    + "PRIMARY KEY(IDNot))";
            stmnt.execute(sql);
            
            sql = "INSERT INTO Departamento (Departamento, Clave) VALUES ('admin', 'admin')";// Creación del usuario Administrador
            stmnt.execute(sql);
            
            System.out.println("Tablas creadas");
        } catch (SQLException e) {
            System.out.println("Error en la creación de las tablas");
            e.printStackTrace();
        }
    }
    
    public void addDepartamento(Departamento d) {
        try {
            sql = "INSERT INTO Departamento VALUES (?, ?)";
            pstmnt = conexion.prepareStatement(sql);
            pstmnt.setString(1, d.getUsuario());
            pstmnt.setString(2, d.getClave());
            pstmnt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error creando el usuario");
            e.printStackTrace();
        }
    }
    
    public void modDepartamento(Departamento d) {
        try {
            sql = "UPDATE Departamento SET Clave=? WHERE Departamento=?";
            pstmnt = conexion.prepareStatement(sql);
            pstmnt.setString(1, d.getClave());
            pstmnt.setString(2, d.getUsuario());
            pstmnt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error modificando el usuario");
            e.printStackTrace();
        }
    }
    
    public void eliminarDepartamento(String departamento) {
        try {
            sql = "DELETE FROM Departamento WHERE Departamento=?";
            pstmnt = conexion.prepareStatement(sql);
            pstmnt.setString(1, departamento);
            pstmnt.executeUpdate();
        } catch (HeadlessException | SQLException e) {
            System.out.println("Error eliminando el usuario");
            e.printStackTrace();
        }
    }
    
    public ArrayList<Departamento> listaUsuarios() {
        ArrayList<Departamento> usuarios = new ArrayList();
        try {
            sql = "SELECT * FROM Departamento";
            stmnt = conexion.createStatement();
            ResultSet rs = stmnt.executeQuery(sql);
            
            while(rs.next()) {
                usuarios.add(new Departamento(rs.getString("Departamento"), rs.getString("Clave")));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println("Error obteniendo los usuarios");
            ex.printStackTrace();
        }
        return usuarios;
    }
    
    public int maxIdNot() {
        int id = 1;
        
        try {
            sql = "SELECT MAX(IdNot) FROM Noticia";
            stmnt = conexion.createStatement();
            ResultSet rs = stmnt.executeQuery(sql);
            
            while(rs.next()) {
                id = rs.getInt("MAX(IdNot)")+1;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return id;
    }
    
    public void addNoticia(Noticia n) {
        try {
            sql = "INSERT INTO Noticia VALUES (?, ?, ?, ?, ?, ?, ? ,?)";
            pstmnt = conexion.prepareStatement(sql);
            pstmnt.setInt(1, n.getIdNoticia());
            pstmnt.setString(2, n.getDepartamento());
            pstmnt.setBytes(3, n.getImagen());
            pstmnt.setString(4, n.getFecha());
            pstmnt.setString(5, n.getRuta());
            pstmnt.setInt(6, n.getDiasVigencia());
            pstmnt.setInt(7, n.getVigente());
            pstmnt.setInt(8, n.getPublica());
            pstmnt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error creando la noticia");
            e.printStackTrace();
        }
    }
    
    public void modNoticia(Noticia n) {
        try {
            sql = "UPDATE Noticia SET Vigente=?, Publica=? WHERE IdNot=?";
            pstmnt = conexion.prepareStatement(sql);
            pstmnt.setInt(1, n.getVigente());
            pstmnt.setInt(2, n.getPublica());
            pstmnt.setInt(3, n.getIdNoticia());
            pstmnt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error modificando la noticia");
            e.printStackTrace();
        }
    }
    
    public void delNoticia(int idNot) {
        try {
            sql = "DELETE FROM Noticia WHERE IdNot=?";
            pstmnt = conexion.prepareStatement(sql);
            pstmnt.setInt(1, idNot);
            pstmnt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error eliminando la noticia");
            e.printStackTrace();
        }
    }
    
    public Noticia getNoticia(int idNot) {
        Noticia noticia = null;
        
        try {
            sql = "SELECT * FROM Noticia WHERE IdNot="+idNot;
            stmnt = conexion.createStatement();
            ResultSet rs = stmnt.executeQuery(sql);
            
            while (rs.next()) {
                noticia = new Noticia(rs.getInt("IdNot"), rs.getInt("DiasVigencia"), rs.getInt("Vigente"), rs.getInt("Publica"), rs.getString("Departamento"), rs.getString("Fecha"), rs.getString("Ruta"), rs.getBytes("Imagen"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return noticia;
    }
    
    public ArrayList<Noticia> listaNoticiasUser(String departamento) {
        ArrayList<Noticia> noticias = new ArrayList();
        try {
            sql = "SELECT IdNot,Fecha,DiasVigencia FROM Noticia WHERE Departamento='"+departamento+"'";
            stmnt = conexion.createStatement();
            ResultSet rs = stmnt.executeQuery(sql);
            
            while(rs.next()) {
                noticias.add(new Noticia(rs.getInt("IdNot"), rs.getString("Fecha"), rs.getInt("DiasVigencia")));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println("Error obteniendo las noticias");
            ex.printStackTrace();
        }
        return noticias;
    }
    
    public ArrayList<Noticia> listaNoticiasAdmin() {
        ArrayList<Noticia> noticias = new ArrayList();
        try {
            sql = "SELECT IdNot,Departamento,Fecha,DiasVigencia,Vigente,Publica FROM Noticia";
            stmnt = conexion.createStatement();
            ResultSet rs = stmnt.executeQuery(sql);
            
            while(rs.next()) {
                noticias.add(new Noticia(rs.getInt("IdNot"), rs.getString("Departamento"), rs.getString("Fecha"), rs.getInt("DiasVigencia"), rs.getInt("Vigente"), rs.getInt("Publica")));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println("Error obteniendo las noticias");
            ex.printStackTrace();
        }
        return noticias;
    }
    
    public void reiniciarBD() {
        try {
            stmnt = conexion.createStatement();
            
            sql = "DROP TABLE Noticia";
            stmnt.execute(sql);
            
            sql = "DROP TABLE Departamento";
            stmnt.execute(sql);
            
            File admin = new File("administrador.dat");
            File usuarios = new File("usuarios.dat");
            
            if (admin.exists())
                admin.delete();
            
            if (usuarios.exists())
                usuarios.delete();
            
            System.out.println("Tablas borradas");
            crearTablas();
        } catch (SQLException e) {
            System.out.println("Error al reiniciar la base de datos");
            e.printStackTrace();
        }
    }
    
    public void cerrar() {
        if (conexion!=null) {
            try {
                // Creación del fichero con el usuario administrador
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File("administrador.dat")));
                stmnt = conexion.createStatement();
                sql = "SELECT * FROM Departamento WHERE Departamento='admin'";
                ResultSet rs = stmnt.executeQuery(sql);
                while(rs.next()) {
                    dos.writeUTF(rs.getString("Departamento")+" "+rs.getString("Clave"));
                }
                rs.close();
                dos.close();
                
                // Creación del fichero con los usuarios normales
                stmnt = conexion.createStatement();
                sql = "SELECT * FROM Departamento WHERE Departamento!='admin'";
                ResultSet rs2 = stmnt.executeQuery(sql);
                if (rs2.next()==true) {
                    rs2.beforeFirst();
                    DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(new File("usuarios.dat")));
                    while(rs2.next()) {
                        dos2.writeUTF(rs2.getString("Departamento")+" "+rs2.getString("Clave"));
                    }
                    rs2.close();
                    dos2.close();
                }else {
                    File usuarios = new File("usuarios.dat");
                    if (usuarios.exists()) {
                        usuarios.delete();
                    }
                    System.out.println("No hay usuarios en la BD");
                }
                
                conexion.close();
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                System.out.println("No se pudo cerrar la conexión.");
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println("Error creando el archivo administrador.dat");
                ex.printStackTrace();
            }
        }
    }

    public Connection getConexion() {
        return conexion;
    }
}
