package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

public class ItemOpcionDTO implements Serializable {

    private final int orden;
    private final String descripcion;

    public ItemOpcionDTO(int orden, String descripcion) {
        this.orden = orden;
        this.descripcion = descripcion;
    }

    public int getOrden() {
        return orden;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
