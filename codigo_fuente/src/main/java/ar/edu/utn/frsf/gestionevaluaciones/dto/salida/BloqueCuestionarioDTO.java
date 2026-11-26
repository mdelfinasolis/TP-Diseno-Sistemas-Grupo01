package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

public class BloqueCuestionarioDTO implements Serializable {

    private final int nroBloque;
    private final int cantidadPreguntas;
    private final int cantidadBloques;

    public BloqueCuestionarioDTO(int nroBloque, int cantidadPreguntas, int cantidadBloques) {
        this.nroBloque = nroBloque;
        this.cantidadPreguntas = cantidadPreguntas;
        this.cantidadBloques = cantidadBloques;
    }

    public int getNroBloque() {
        return nroBloque;
    }

    public int getCantidadPreguntas() {
        return cantidadPreguntas;
    }

    public int getCantidadBloques() {
        return cantidadBloques;
    }
}
