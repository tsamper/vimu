package org.tsamper.proyecto_final.modelo;

import java.util.HashSet;
/**
 * Clase Festival que hereda de Evento
 */
public class Festival extends Evento{
    private HashSet<Grupo> grupos;

    public HashSet<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(HashSet<Grupo> grupos) {
        this.grupos = grupos;
    }
}
