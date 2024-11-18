package org.tsamper.proyecto_final.modelo;


/**
 * Clase Usuario
 */
public class Usuario {
    private int id;
    private String nombre;
    private String apellidos;
    private String email;
    private String nomUsuario;
    private String contrasenya;
    private GrupoUsuarios grupoUsuarios;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomUsuario() {
        return nomUsuario;
    }

    public void setNomUsuario(String nomUsuario) {
        this.nomUsuario = nomUsuario;
    }

    public String getContrasenya() {
        return contrasenya;
    }

    public void setContrasenya(String contrasenya) {
        this.contrasenya = contrasenya;
    }

    public GrupoUsuarios getGrupoUsuarios() {
        return grupoUsuarios;
    }

    public void setGrupoUsuarios(GrupoUsuarios grupoUsuarios) {
        this.grupoUsuarios = grupoUsuarios;
    }
}
