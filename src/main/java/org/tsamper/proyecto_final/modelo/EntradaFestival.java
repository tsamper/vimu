package org.tsamper.proyecto_final.modelo;
/**
 * Clase EntradaFestival que hereda de Entrada
 */
public class EntradaFestival extends Entrada {
    private Festival festival;

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
    }
}
