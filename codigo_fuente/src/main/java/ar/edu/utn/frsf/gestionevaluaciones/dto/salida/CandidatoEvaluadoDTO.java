package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;

import java.io.Serializable;
import java.time.LocalDate;

public class CandidatoEvaluadoDTO implements Serializable {

    private final String apellido;
    private final String nombre;
    private final TIPODOCUMENTO tipoDocumento;
    private final String nroDocumento;
    private final int nroCandidato;
    private final LocalDate fechaFinalizacionCuestionario;

    public CandidatoEvaluadoDTO(String apellido, String nombre, TIPODOCUMENTO tipoDocumento, String nroDocumento,
                                int nroCandidato, LocalDate fechaFinalizacionCuestionario) {
        this.apellido = apellido;
        this.nombre = nombre;
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.nroCandidato = nroCandidato;
        this.fechaFinalizacionCuestionario = fechaFinalizacionCuestionario;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public TIPODOCUMENTO getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public int getNroCandidato() {
        return nroCandidato;
    }

    public LocalDate getFechaFinalizacionCuestionario() {
        return fechaFinalizacionCuestionario;
    }
}
