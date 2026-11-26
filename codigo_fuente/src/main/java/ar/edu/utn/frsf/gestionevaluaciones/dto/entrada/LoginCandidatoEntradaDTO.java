package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;

import java.io.Serializable;

public class LoginCandidatoEntradaDTO implements Serializable {

    private TIPODOCUMENTO tipoDocumento;
    private String nroDocumento;
    private String claveIngreso;

    public LoginCandidatoEntradaDTO() {
    }

    public LoginCandidatoEntradaDTO(TIPODOCUMENTO tipoDocumento, String nroDocumento, String claveIngreso) {
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.claveIngreso = claveIngreso;
    }

    public TIPODOCUMENTO getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TIPODOCUMENTO tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public void setNroDocumento(String nroDocumento) {
        this.nroDocumento = nroDocumento;
    }

    public String getClaveIngreso() {
        return claveIngreso;
    }

    public void setClaveIngreso(String claveIngreso) {
        this.claveIngreso = claveIngreso;
    }
}
