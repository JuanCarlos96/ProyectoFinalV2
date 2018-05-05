/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectofinal;

/**
 *
 * @author juancarlos
 */
public class Noticia {
    private int idNoticia, diasVigencia, vigente, publica;
    private String departamento, fecha, ruta;
    private byte[] imagen;

    /**
     * Contructor con todos los campos para realizar la inserción en la base de datos
     */
    public Noticia(int idNoticia, int diasVigencia, int vigente, int publica, String departamento, String fecha, String ruta, byte[] imagen) {
        this.idNoticia = idNoticia;
        this.diasVigencia = diasVigencia;
        this.vigente = vigente;
        this.publica = publica;
        this.departamento = departamento;
        this.fecha = fecha;
        this.ruta = ruta;
        this.imagen = imagen;
    }

    /**
     * Contructor para listar las noticias en el perfil del usuario
     */
    public Noticia(int idNoticia, String fecha, int diasVigencia) {
        this.idNoticia = idNoticia;
        this.fecha = fecha;
        this.diasVigencia = diasVigencia;
    }

    /**
     * Constructor para listar las noticias en el perfil del administrador
     */
    public Noticia(int idNoticia, String departamento, String fecha, int diasVigencia, int vigente, int publica) {
        this.idNoticia = idNoticia;
        this.departamento = departamento;
        this.fecha = fecha;
        this.diasVigencia = diasVigencia;
        this.vigente = vigente;
        this.publica = publica;
    }
    
    /**
     * Contructor para modificar las noticias desde el perfil del administrador
     */
    public Noticia(int idNot, int vigente, int publica) {
        this.idNoticia = idNot;
        this.vigente = vigente;
        this.publica = publica;
    }

    public int getIdNoticia() {
        return idNoticia;
    }

    public void setIdNoticia(int idNoticia) {
        this.idNoticia = idNoticia;
    }

    public int getDiasVigencia() {
        return diasVigencia;
    }

    public void setDiasVigencia(int diasVigencia) {
        this.diasVigencia = diasVigencia;
    }

    public int getVigente() {
        return vigente;
    }

    public void setVigente(int vigente) {
        this.vigente = vigente;
    }

    public int getPublica() {
        return publica;
    }

    public void setPublica(int publica) {
        this.publica = publica;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }
}
