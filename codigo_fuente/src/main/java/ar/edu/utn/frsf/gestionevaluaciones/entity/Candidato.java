package ar.edu.utn.frsf.gestionevaluaciones.entity;

import ar.edu.utn.frsf.gestionevaluaciones.interfaces.IAutenticable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Candidato (diagrama de clases / DER). Según el DER su clave es nroCandidato,
 * que es a la vez el "id" heredado de Usuario (misma columna, solo lectura aquí).
 */
@Entity
@Table(name = "candidato")
@AttributeOverride(name = "id", column = @Column(name = "nro_candidato"))
public class Candidato extends Usuario implements IAutenticable {

    @Column(name = "nro_candidato", insertable = false, updatable = false)
    private Integer nroCandidato;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo_documento", nullable = false, length = 3)
    private TIPODOCUMENTO tipoDocumento;

    @Column(name = "nro_documento", nullable = false, length = 20)
    private String nroDocumento;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "genero", length = 1)
    private GENERO genero;

    @Column(name = "nacionalidad", length = 50)
    private String nacionalidad;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "escolaridad", length = 100)
    private String escolaridad;

    @Column(name = "eliminado", nullable = false)
    private boolean eliminado;

    /** Candidato -- Cuestionario : responde */
    @OneToMany(mappedBy = "candidato")
    private List<Cuestionario> cuestionarios = new ArrayList<>();

    protected Candidato() {
    }

    /** CU001 3.A.2: el tipo y número de documento ingresados corresponden a este candidato. */
    public boolean coincideDocumento(TIPODOCUMENTO tipo, String nroDocumento) {
        return tipo != null && nroDocumento != null
                && tipo == this.tipoDocumento
                && nroDocumento.trim().equalsIgnoreCase(this.nroDocumento);
    }

    /**
     * CU001 3.A.2 (vía IAutenticable): la clave corresponde a un cuestionario del candidato
     * que está ACTIVO o EN_PROCESO.
     */
    @Override
    public boolean validarCredenciales(String clave) {
        if (clave == null || clave.isBlank()) {
            return false;
        }
        for (Cuestionario cuestionario : getCuestionarios()) {
            DetalleCuestionario detalle = cuestionario.getDetalle();
            ESTADOCUESTIONARIO estado = detalle.getEstado();
            boolean activoOEnProceso = estado == ESTADOCUESTIONARIO.ACTIVO || estado == ESTADOCUESTIONARIO.EN_PROCESO;
            if (activoOEnProceso && cuestionario.getClave().equalsIgnoreCase(clave.trim())) {
                return true;
            }
        }
        return false;
    }

    public List<Cuestionario> getCuestionarios() {
        return cuestionarios;
    }

    public Integer getNroCandidato() {
        return nroCandidato;
    }

    public TIPODOCUMENTO getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public GENERO getGenero() {
        return genero;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public String getEmail() {
        return email;
    }

    public String getEscolaridad() {
        return escolaridad;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
}
