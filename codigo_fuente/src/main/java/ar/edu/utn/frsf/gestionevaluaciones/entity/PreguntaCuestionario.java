package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Pregunta asignada a un cuestionario, con su orden y el bloque (paso del wizard) en que se muestra. */
@Entity
@Table(name = "pregunta_cuestionario")
public class PreguntaCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta_cuestionario")
    private Integer id;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(name = "nro_bloque", nullable = false)
    private int nroBloque;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clave_cuestionario", nullable = false)
    private Cuestionario cuestionario;

    /** PreguntaCuestionario "0..*" --> "1" Pregunta : referencia */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_pregunta", nullable = false)
    private Pregunta pregunta;

    @OneToOne(mappedBy = "preguntaCuestionario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RespuestaSeleccionada respuesta;

    protected PreguntaCuestionario() {
    }

    public PreguntaCuestionario(Pregunta pregunta, int orden, int nroBloque) {
        this.pregunta = pregunta;
        this.orden = orden;
        this.nroBloque = nroBloque;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    /** Crea la RespuestaSeleccionada asociada al ítem elegido (reemplaza una anterior si existiera). */
    public void registrarRespuesta(ItemOpcion item, LocalDateTime fechaRespuesta) {
        RespuestaSeleccionada nueva = new RespuestaSeleccionada(item, fechaRespuesta);
        nueva.setPreguntaCuestionario(this);
        this.respuesta = nueva;
    }

    public RespuestaSeleccionada getRespuesta() {
        return respuesta;
    }

    public int getOrden() {
        return orden;
    }

    public int getNroBloque() {
        return nroBloque;
    }

    void setCuestionario(Cuestionario cuestionario) {
        this.cuestionario = cuestionario;
    }
}
