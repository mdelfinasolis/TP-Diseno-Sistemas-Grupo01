package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Cuestionario. Su identificador es la clave de ingreso (IDAO&lt;Cuestionario, String&gt;). */
@Entity
@Table(name = "cuestionario")
public class Cuestionario {

    @Id
    @Column(name = "clave", length = 20)
    private String clave;

    /** Tiempo máximo para completarlo, en minutos (se toma del repositorio de configuraciones al crearlo). */
    @Column(name = "tiempo_maximo", nullable = false)
    private int tiempoMaximo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private Evaluacion evaluacion;

    /** Cuestionario "0..*" --> "1" Candidato : pertenece a */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nro_candidato", nullable = false)
    private Candidato candidato;

    @Embedded
    private DetalleCuestionario detalle;

    /** Cuestionario "1" *-- "1..*" PreguntaCuestionario : incluye */
    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden")
    private List<PreguntaCuestionario> preguntas = new ArrayList<>();

    /** Cuestionario "1" *-- "0..*" ResultadoFactor : calcula */
    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoFactor> resultadosFactor = new ArrayList<>();

    /** Cuestionario "1" *-- "0..*" ResultadoCompetencia : calcula */
    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoCompetencia> resultadosCompetencia = new ArrayList<>();

    protected Cuestionario() {
    }

    public Cuestionario(Evaluacion evaluacion, Candidato candidato, String clave, int tiempoMaximo) {
        this.evaluacion = evaluacion;
        this.candidato = candidato;
        this.clave = clave;
        this.tiempoMaximo = tiempoMaximo;
        this.detalle = new DetalleCuestionario(ESTADOCUESTIONARIO.ACTIVO);
    }

    public DetalleCuestionario getDetalle() {
        return detalle;
    }

    public Candidato getCandidato() {
        return candidato;
    }

    public String getClave() {
        return clave;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public int getTiempoMaximo() {
        return tiempoMaximo;
    }

    public void agregarPreguntaCuestionario(PreguntaCuestionario pc) {
        pc.setCuestionario(this);
        preguntas.add(pc);
    }

    /** Preguntas de un bloque (paso del wizard), en su orden. */
    public List<PreguntaCuestionario> getPreguntasBloque(int nroBloque) {
        return preguntas.stream()
                .filter(pc -> pc.getNroBloque() == nroBloque)
                .toList();
    }

    public PreguntaCuestionario getPreguntaCuestionario(String codigoPregunta) {
        return preguntas.stream()
                .filter(pc -> pc.getPregunta().getCodigo().equals(codigoPregunta))
                .findFirst()
                .orElse(null);
    }

    public List<PreguntaCuestionario> getPreguntas() {
        return preguntas;
    }

    public void agregarResultadoFactor(ResultadoFactor resultado) {
        resultado.setCuestionario(this);
        resultadosFactor.add(resultado);
    }

    public void agregarResultadoCompetencia(ResultadoCompetencia resultado) {
        resultado.setCuestionario(this);
        resultadosCompetencia.add(resultado);
    }

    public List<ResultadoCompetencia> getResultadosCompetencia() {
        return resultadosCompetencia;
    }

    public List<ResultadoFactor> getResultadosFactor() {
        return resultadosFactor;
    }
}
