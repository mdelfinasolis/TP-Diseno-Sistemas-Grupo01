package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

public class OpcionRespuestaListadoDTO implements Serializable {

    private final String nombre;
    private final String descripcion;

    public OpcionRespuestaListadoDTO(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
