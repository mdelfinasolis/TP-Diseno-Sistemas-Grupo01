package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/** Relación "requiere" del DER entre Puesto y Competencia, con su ponderación mínima (0 a 10). */
@Entity
@Table(name = "puesto_competencia")
@IdClass(PuestoCompetencia.PuestoCompetenciaId.class)
public class PuestoCompetencia {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_puesto")
    private Puesto puesto;

    /** PuestoCompetencia "0..*" --> "1" Competencia : referencia */
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_competencia")
    private Competencia competencia;

    @Column(name = "ponderacion_minima", nullable = false)
    private int ponderacionMinima;

    protected PuestoCompetencia() {
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public int getPonderacionMinima() {
        return ponderacionMinima;
    }

    /** Clave compuesta de PuestoCompetencia. */
    public static class PuestoCompetenciaId implements Serializable {
        private String puesto;
        private String competencia;

        public PuestoCompetenciaId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PuestoCompetenciaId that)) return false;
            return Objects.equals(puesto, that.puesto) && Objects.equals(competencia, that.competencia);
        }

        @Override
        public int hashCode() {
            return Objects.hash(puesto, competencia);
        }
    }
}
