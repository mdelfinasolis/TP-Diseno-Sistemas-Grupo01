package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

public class PuestoListadoDTO implements Serializable {

    private final String codigo;
    private final String nombre;
    private final String empresa;

    public PuestoListadoDTO(String codigo, String nombre, String empresa) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.empresa = empresa;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmpresa() {
        return empresa;
    }
}
