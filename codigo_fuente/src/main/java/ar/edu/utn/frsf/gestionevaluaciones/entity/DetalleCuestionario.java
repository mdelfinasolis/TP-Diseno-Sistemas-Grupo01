package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Clase de asociación (Candidato, Cuestionario) del diagrama de clases.
 * En el DER sus atributos son columnas de la tabla cuestionario, por eso se mapea como @Embeddable.
 */
@Embeddable
public class DetalleCuestionario {

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 20)
    private ESTADOCUESTIONARIO estado;

    @Column(name = "cantidad_accesos", nullable = false)
    private int cantidadAccesos;

    @Column(name = "ultimo_ingreso")
    private LocalDateTime ultimoIngreso;

    protected DetalleCuestionario() {
    }

    public DetalleCuestionario(ESTADOCUESTIONARIO estado) {
        this.estado = estado;
    }

    public ESTADOCUESTIONARIO getEstado() {
        return estado;
    }

    public void setEstado(ESTADOCUESTIONARIO estado) {
        this.estado = estado;
    }

    public void registrarAcceso(LocalDateTime fechaHora) {
        this.cantidadAccesos++;
        this.ultimoIngreso = fechaHora;
    }

    public void registrarInicio(LocalDateTime fechaHora) {
        this.fechaInicio = fechaHora;
    }

    public void registrarFin(LocalDateTime fechaHora) {
        this.fechaFin = fechaHora;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public int getCantidadAccesos() {
        return cantidadAccesos;
    }

    public LocalDateTime getUltimoIngreso() {
        return ultimoIngreso;
    }
}
