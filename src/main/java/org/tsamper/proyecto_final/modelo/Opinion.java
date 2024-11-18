package org.tsamper.proyecto_final.modelo;

import org.tsamper.proyecto_final.modelo.enums.OpcionesOpinion;

import java.time.LocalDate;
/**
 * Clase Opinion
 */
public class Opinion {
    private int id;
    private Usuario usuario;
    private Grupo grupo;
    private Concierto concierto;
    private String comentario;
    private LocalDate fecha;
    private OpcionesOpinion recomendado;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Concierto getConcierto() {
        return concierto;
    }

    public void setConcierto(Concierto concierto) {
        this.concierto = concierto;
    }

    public OpcionesOpinion getRecomendado() {
        return recomendado;
    }

    public void setRecomendado(OpcionesOpinion recomendado) {
        this.recomendado = recomendado;
    }
}
