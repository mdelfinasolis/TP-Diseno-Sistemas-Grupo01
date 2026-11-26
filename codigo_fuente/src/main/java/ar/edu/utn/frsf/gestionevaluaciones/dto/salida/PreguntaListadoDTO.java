package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

public class PreguntaListadoDTO implements Serializable {

    private final String codigo;
    private final String competencia;
    private final String factor;
    private final String pregunta;

    public PreguntaListadoDTO(String codigo, String competencia, String factor, String pregunta) {
        this.codigo = codigo;
        this.competencia = competencia;
        this.factor = factor;
        this.pregunta = pregunta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCompetencia() {
        return competencia;
    }

    public String getFactor() {
        return factor;
    }

    public String getPregunta() {
        return pregunta;
    }
}
