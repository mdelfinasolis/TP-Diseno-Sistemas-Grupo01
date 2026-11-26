package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

public class FiltroFactorEntradaDTO implements Serializable {

    private String codigoCompetencia;
    private String nombre;

    public FiltroFactorEntradaDTO() {
    }

    public FiltroFactorEntradaDTO(String codigoCompetencia, String nombre) {
        this.codigoCompetencia = codigoCompetencia;
        this.nombre = nombre;
    }

    public String getCodigoCompetencia() {
        return codigoCompetencia;
    }

    public void setCodigoCompetencia(String codigoCompetencia) {
        this.codigoCompetencia = codigoCompetencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
