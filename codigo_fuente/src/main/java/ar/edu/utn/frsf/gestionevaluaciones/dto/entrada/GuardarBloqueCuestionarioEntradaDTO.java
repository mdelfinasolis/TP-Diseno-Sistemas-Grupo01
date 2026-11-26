package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GuardarBloqueCuestionarioEntradaDTO implements Serializable {

    private String claveCuestionario;
    private int nroBloque;
    private List<RespuestaPreguntaEntradaDTO> respuestas = new ArrayList<>();

    public GuardarBloqueCuestionarioEntradaDTO() {
    }

    public GuardarBloqueCuestionarioEntradaDTO(String claveCuestionario, int nroBloque,
                                               List<RespuestaPreguntaEntradaDTO> respuestas) {
        this.claveCuestionario = claveCuestionario;
        this.nroBloque = nroBloque;
        this.respuestas = respuestas != null ? respuestas : new ArrayList<>();
    }

    public String getClaveCuestionario() {
        return claveCuestionario;
    }

    public void setClaveCuestionario(String claveCuestionario) {
        this.claveCuestionario = claveCuestionario;
    }

    public int getNroBloque() {
        return nroBloque;
    }

    public void setNroBloque(int nroBloque) {
        this.nroBloque = nroBloque;
    }

    public List<RespuestaPreguntaEntradaDTO> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<RespuestaPreguntaEntradaDTO> respuestas) {
        this.respuestas = respuestas != null ? respuestas : new ArrayList<>();
    }
}
