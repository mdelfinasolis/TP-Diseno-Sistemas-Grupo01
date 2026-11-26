package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;
import java.time.LocalDate;

public class EvaluacionListadoDTO implements Serializable {

    private final int idEvaluacion;
    private final LocalDate fechaCreacion;
    private final int cantidadCandidatos;

    public EvaluacionListadoDTO(int idEvaluacion, LocalDate fechaCreacion, int cantidadCandidatos) {
        this.idEvaluacion = idEvaluacion;
        this.fechaCreacion = fechaCreacion;
        this.cantidadCandidatos = cantidadCandidatos;
    }

    public int getIdEvaluacion() {
        return idEvaluacion;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public int getCantidadCandidatos() {
        return cantidadCandidatos;
    }
}
