package org.tsamper.proyecto_final.modelo;

import org.tsamper.proyecto_final.modelo.enums.Privilegios;
/**
 * Clase GrupoUsuarios
 */
public class GrupoUsuarios {
    private int id;
    private Privilegios tipo;

    public GrupoUsuarios(Privilegios tipo) {
        this.tipo = tipo;
    }

    public GrupoUsuarios() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Privilegios getTipo() {
        return tipo;
    }

    public void setTipo(Privilegios tipo) {
        this.tipo = tipo;
    }
}
