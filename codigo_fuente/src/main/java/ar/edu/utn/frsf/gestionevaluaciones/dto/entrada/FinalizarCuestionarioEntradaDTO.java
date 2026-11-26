package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

public class FinalizarCuestionarioEntradaDTO implements Serializable {

    private String claveCuestionario;

    public FinalizarCuestionarioEntradaDTO() {
    }

    public FinalizarCuestionarioEntradaDTO(String claveCuestionario) {
        this.claveCuestionario = claveCuestionario;
    }

    public String getClaveCuestionario() {
        return claveCuestionario;
    }

    public void setClaveCuestionario(String claveCuestionario) {
        this.claveCuestionario = claveCuestionario;
    }
}
