package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

public class FiltroPreguntaEntradaDTO implements Serializable {

    private String codigoCompetencia;
    private String codigoFactor;
    private String nombre;

    public FiltroPreguntaEntradaDTO() {
    }

    public FiltroPreguntaEntradaDTO(String codigoCompetencia, String codigoFactor, String nombre) {
        this.codigoCompetencia = codigoCompetencia;
        this.codigoFactor = codigoFactor;
        this.nombre = nombre;
    }

    public String getCodigoCompetencia() {
        return codigoCompetencia;
    }

    public void setCodigoCompetencia(String codigoCompetencia) {
        this.codigoCompetencia = codigoCompetencia;
    }

    public String getCodigoFactor() {
        return codigoFactor;
    }

    public void setCodigoFactor(String codigoFactor) {
        this.codigoFactor = codigoFactor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
