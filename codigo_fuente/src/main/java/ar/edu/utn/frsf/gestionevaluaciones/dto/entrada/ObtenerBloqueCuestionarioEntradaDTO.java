package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

public class ObtenerBloqueCuestionarioEntradaDTO implements Serializable {

    private String claveCuestionario;
    private int nroBloque;

    public ObtenerBloqueCuestionarioEntradaDTO() {
    }

    public ObtenerBloqueCuestionarioEntradaDTO(String claveCuestionario, int nroBloque) {
        this.claveCuestionario = claveCuestionario;
        this.nroBloque = nroBloque;
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
}
