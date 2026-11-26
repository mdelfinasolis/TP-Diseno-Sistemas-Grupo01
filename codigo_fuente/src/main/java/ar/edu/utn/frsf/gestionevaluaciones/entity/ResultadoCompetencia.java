package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Relación "obtiene_resultado_en" del DER entre Cuestionario y Competencia (atributo total). */
@Entity
@Table(name = "resultado_competencia")
public class ResultadoCompetencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado_competencia")
    private Integer id;

    @Column(name = "total", nullable = false)
    private double total;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clave_cuestionario", nullable = false)
    private Cuestionario cuestionario;

    /** ResultadoCompetencia "0..*" --> "1" Competencia : corresponde a */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_competencia", nullable = false)
    private Competencia competencia;

    protected ResultadoCompetencia() {
    }

    public ResultadoCompetencia(Competencia competencia, double total) {
        this.competencia = competencia;
        this.total = total;
    }

    public double getTotal() {
        return total;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    void setCuestionario(Cuestionario cuestionario) {
        this.cuestionario = cuestionario;
    }
}
