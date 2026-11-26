package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

/** Competencia requerida por un puesto. evaluable lo asigna GestorEvaluaciones (CU025). */
public class CompetenciaRequeridaDTO implements Serializable {

    private final String codigoCompetencia;
    private final String competencia;
    private final int ponderacionRequerida;
    private boolean evaluable;

    public CompetenciaRequeridaDTO(String codigoCompetencia, String competencia, int ponderacionRequerida,
                                   boolean evaluable) {
        this.codigoCompetencia = codigoCompetencia;
        this.competencia = competencia;
        this.ponderacionRequerida = ponderacionRequerida;
        this.evaluable = evaluable;
    }

    public String getCodigoCompetencia() {
        return codigoCompetencia;
    }

    public String getCompetencia() {
        return competencia;
    }

    public int getPonderacionRequerida() {
        return ponderacionRequerida;
    }

    public boolean isEvaluable() {
        return evaluable;
    }

    public void setEvaluable(boolean evaluable) {
        this.evaluable = evaluable;
    }
}
