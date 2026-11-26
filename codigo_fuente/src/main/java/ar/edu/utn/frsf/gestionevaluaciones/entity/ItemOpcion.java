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

/** Ítem de una opción de respuesta. Clave: (opción de respuesta, orden). */
@Entity
@Table(name = "item_opcion")
@IdClass(ItemOpcion.ItemOpcionId.class)
public class ItemOpcion {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nombre_opcion_respuesta")
    private OpcionRespuesta opcionRespuesta;

    @Id
    @Column(name = "orden")
    private int orden;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    protected ItemOpcion() {
    }

    public int getOrden() {
        return orden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public OpcionRespuesta getOpcionRespuesta() {
        return opcionRespuesta;
    }

    /** Clave compuesta de ItemOpcion. */
    public static class ItemOpcionId implements Serializable {
        private String opcionRespuesta;
        private int orden;

        public ItemOpcionId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemOpcionId that)) return false;
            return orden == that.orden && Objects.equals(opcionRespuesta, that.opcionRespuesta);
        }

        @Override
        public int hashCode() {
            return Objects.hash(opcionRespuesta, orden);
        }
    }
}
