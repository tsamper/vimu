package org.tsamper.proyecto_final.modelo;

/**
 * Clase Concierto que hereda de Evento
 */
public class Concierto extends Evento{
    private Grupo grupo;

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }



}
