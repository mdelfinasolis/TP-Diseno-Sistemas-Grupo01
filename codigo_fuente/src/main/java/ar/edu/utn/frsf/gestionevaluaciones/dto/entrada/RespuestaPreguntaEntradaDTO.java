package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

public class RespuestaPreguntaEntradaDTO implements Serializable {

    private String codigoPregunta;
    private int ordenItemSeleccionado;

    public RespuestaPreguntaEntradaDTO() {
    }

    public RespuestaPreguntaEntradaDTO(String codigoPregunta, int ordenItemSeleccionado) {
        this.codigoPregunta = codigoPregunta;
        this.ordenItemSeleccionado = ordenItemSeleccionado;
    }

    public String getCodigoPregunta() {
        return codigoPregunta;
    }

    public void setCodigoPregunta(String codigoPregunta) {
        this.codigoPregunta = codigoPregunta;
    }

    public int getOrdenItemSeleccionado() {
        return ordenItemSeleccionado;
    }

    public void setOrdenItemSeleccionado(int ordenItemSeleccionado) {
        this.ordenItemSeleccionado = ordenItemSeleccionado;
    }
}
