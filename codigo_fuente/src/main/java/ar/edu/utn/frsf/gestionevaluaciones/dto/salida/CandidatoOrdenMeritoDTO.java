package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Fila del orden de mérito. puntajeObtenido solo tiene valor para cuestionarios COMPLETO
 * (para el resto queda en null y la pantalla no lo muestra).
 */
public class CandidatoOrdenMeritoDTO implements Serializable {

    private final TIPODOCUMENTO tipoDocumento;
    private final String nroDocumento;
    private final String apellido;
    private final String nombre;
    private final ESTADOCUESTIONARIO estadoCuestionario;
    private final Double puntajeObtenido;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final LocalDateTime ultimoIngreso;
    private final int cantidadAccesos;
    private final boolean cumpleMinimos;

    public CandidatoOrdenMeritoDTO(TIPODOCUMENTO tipoDocumento, String nroDocumento, String apellido, String nombre,
                                   ESTADOCUESTIONARIO estadoCuestionario, Double puntajeObtenido,
                                   LocalDateTime fechaInicio, LocalDateTime fechaFin, LocalDateTime ultimoIngreso,
                                   int cantidadAccesos, boolean cumpleMinimos) {
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.apellido = apellido;
        this.nombre = nombre;
        this.estadoCuestionario = estadoCuestionario;
        this.puntajeObtenido = puntajeObtenido;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.ultimoIngreso = ultimoIngreso;
        this.cantidadAccesos = cantidadAccesos;
        this.cumpleMinimos = cumpleMinimos;
    }

    public TIPODOCUMENTO getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public ESTADOCUESTIONARIO getEstadoCuestionario() {
        return estadoCuestionario;
    }

    public Double getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public LocalDateTime getUltimoIngreso() {
        return ultimoIngreso;
    }

    public int getCantidadAccesos() {
        return cantidadAccesos;
    }

    public boolean isCumpleMinimos() {
        return cumpleMinimos;
    }
}
