package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Respuesta (0..1) de una PreguntaCuestionario: el ítem de la opción de respuesta elegido. */
@Entity
@Table(name = "respuesta_seleccionada")
public class RespuestaSeleccionada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta_seleccionada")
    private Integer id;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fechaRespuesta;

    /** PreguntaCuestionario "1" -- "0..1" RespuestaSeleccionada : posee */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta_cuestionario", nullable = false, unique = true)
    private PreguntaCuestionario preguntaCuestionario;

    /** RespuestaSeleccionada "0..*" --> "1" ItemOpcion : selecciona */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "nombre_opcion_respuesta", referencedColumnName = "nombre_opcion_respuesta", nullable = false),
            @JoinColumn(name = "orden_item", referencedColumnName = "orden", nullable = false)
    })
    private ItemOpcion item;

    protected RespuestaSeleccionada() {
    }

    public RespuestaSeleccionada(ItemOpcion item, LocalDateTime fechaRespuesta) {
        this.item = item;
        this.fechaRespuesta = fechaRespuesta;
    }

    public ItemOpcion getItem() {
        return item;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    void setPreguntaCuestionario(PreguntaCuestionario preguntaCuestionario) {
        this.preguntaCuestionario = preguntaCuestionario;
    }
}
