package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "opcion_respuesta")
public class OpcionRespuesta {

    @Id
    @Column(name = "nombre_opcion_respuesta", length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "eliminada", nullable = false)
    private boolean eliminada;

    /** OpcionRespuesta "1" *-- "2..*" ItemOpcion : define */
    @OneToMany(mappedBy = "opcionRespuesta")
    @OrderBy("orden")
    private List<ItemOpcion> items = new ArrayList<>();

    protected OpcionRespuesta() {
    }

    public ItemOpcion getItem(int orden) {
        return items.stream().filter(i -> i.getOrden() == orden).findFirst().orElse(null);
    }

    public List<ItemOpcion> getItems() {
        return items;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEliminada() {
        return eliminada;
    }

    public void setEliminada(boolean eliminada) {
        this.eliminada = eliminada;
    }
}
