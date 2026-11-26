package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

public class FactorListadoDTO implements Serializable {

    private final String codigo;
    private final String nombre;
    private final String competencia;

    public FactorListadoDTO(String codigo, String nombre, String competencia) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.competencia = competencia;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCompetencia() {
        return competencia;
    }
}
