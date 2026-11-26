package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;
import java.util.List;

public class OpcionRespuestaDetalleDTO implements Serializable {

    private final String nombre;
    private final String descripcion;
    private final List<ItemOpcionDTO> items;

    public OpcionRespuestaDetalleDTO(String nombre, String descripcion, List<ItemOpcionDTO> items) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.items = items;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<ItemOpcionDTO> getItems() {
        return items;
    }
}
