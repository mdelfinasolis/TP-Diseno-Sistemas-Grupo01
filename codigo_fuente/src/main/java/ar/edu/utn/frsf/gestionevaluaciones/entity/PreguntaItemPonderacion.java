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
import jakarta.persistence.Table;

/** Clase de asociación (Pregunta, ItemOpcion): ponderación que otorga cada ítem de la opción de respuesta. */
@Entity
@Table(name = "pregunta_item_ponderacion")
public class PreguntaItemPonderacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta_item_ponderacion")
    private Integer id;

    @Column(name = "ponderacion", nullable = false)
    private double ponderacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_pregunta", nullable = false)
    private Pregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "nombre_opcion_respuesta", referencedColumnName = "nombre_opcion_respuesta", nullable = false),
            @JoinColumn(name = "orden_item", referencedColumnName = "orden", nullable = false)
    })
    private ItemOpcion item;

    protected PreguntaItemPonderacion() {
    }

    public PreguntaItemPonderacion(Pregunta pregunta, ItemOpcion item, double ponderacion) {
        this.pregunta = pregunta;
        this.item = item;
        this.ponderacion = ponderacion;
    }

    public double getPonderacion() {
        return ponderacion;
    }

    public ItemOpcion getItem() {
        return item;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }
}
