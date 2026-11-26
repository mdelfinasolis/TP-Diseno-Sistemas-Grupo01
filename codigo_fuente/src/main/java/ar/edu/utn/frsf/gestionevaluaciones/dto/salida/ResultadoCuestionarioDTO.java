package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ResultadoCuestionarioDTO implements Serializable {

    private final ESTADOCUESTIONARIO estadoFinal;
    private final LocalDateTime fechaFinalizacion;
    private final String mensaje;

    public ResultadoCuestionarioDTO(ESTADOCUESTIONARIO estadoFinal, LocalDateTime fechaFinalizacion, String mensaje) {
        this.estadoFinal = estadoFinal;
        this.fechaFinalizacion = fechaFinalizacion;
        this.mensaje = mensaje;
    }

    public ESTADOCUESTIONARIO getEstadoFinal() {
        return estadoFinal;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public String getMensaje() {
        return mensaje;
    }
}
