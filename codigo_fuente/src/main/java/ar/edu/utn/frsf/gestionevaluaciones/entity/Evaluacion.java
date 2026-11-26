package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Integer id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "cant_preguntas_por_bloque", nullable = false)
    private int cantPreguntasPorBloque;

    /** Evaluacion "0..*" --> "1" Puesto : evalua */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_puesto", nullable = false)
    private Puesto puesto;

    /** Evaluacion "1" *-- "1..*" Cuestionario : genera */
    @OneToMany(mappedBy = "evaluacion")
    private List<Cuestionario> cuestionarios = new ArrayList<>();

    protected Evaluacion() {
    }

    public Evaluacion(Puesto puesto, LocalDate fechaCreacion, int cantPreguntasPorBloque) {
        this.puesto = puesto;
        this.fechaCreacion = fechaCreacion;
        this.cantPreguntasPorBloque = cantPreguntasPorBloque;
    }

    public Integer getId() {
        return id;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public int getCantPreguntasPorBloque() {
        return cantPreguntasPorBloque;
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public List<Cuestionario> getCuestionarios() {
        return cuestionarios;
    }

    /** Cantidad de candidatos evaluados: uno por cuestionario generado. */
    public int getCantidadCandidatos() {
        return cuestionarios.size();
    }
}
