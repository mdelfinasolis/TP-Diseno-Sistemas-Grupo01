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

/** Relación "obtiene_resultado_en" del DER entre Cuestionario y Factor (atributo total). */
@Entity
@Table(name = "resultado_factor")
public class ResultadoFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado_factor")
    private Integer id;

    @Column(name = "total", nullable = false)
    private double total;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clave_cuestionario", nullable = false)
    private Cuestionario cuestionario;

    /** ResultadoFactor "0..*" --> "1" Factor : corresponde a */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_factor", nullable = false)
    private Factor factor;

    protected ResultadoFactor() {
    }

    public ResultadoFactor(Factor factor, double total) {
        this.factor = factor;
        this.total = total;
    }

    public double getTotal() {
        return total;
    }

    public Factor getFactor() {
        return factor;
    }

    void setCuestionario(Cuestionario cuestionario) {
        this.cuestionario = cuestionario;
    }
}
