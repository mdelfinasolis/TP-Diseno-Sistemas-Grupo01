package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

public class PreguntaCuestionarioDTO implements Serializable {

    private final String codigoPregunta;
    private final String enunciado;

    public PreguntaCuestionarioDTO(String codigoPregunta, String enunciado) {
        this.codigoPregunta = codigoPregunta;
        this.enunciado = enunciado;
    }

    public String getCodigoPregunta() {
        return codigoPregunta;
    }

    public String getEnunciado() {
        return enunciado;
    }
}
