package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;

import java.io.Serializable;

public class CandidatoClaveDTO implements Serializable {

    private final int nroCandidato;
    private final String apellido;
    private final String nombre;
    private final TIPODOCUMENTO tipoDocumento;
    private final String nroDocumento;
    private final String claveIngreso;

    public CandidatoClaveDTO(int nroCandidato, String apellido, String nombre, TIPODOCUMENTO tipoDocumento,
                             String nroDocumento, String claveIngreso) {
        this.nroCandidato = nroCandidato;
        this.apellido = apellido;
        this.nombre = nombre;
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.claveIngreso = claveIngreso;
    }

    public int getNroCandidato() {
        return nroCandidato;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public TIPODOCUMENTO getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public String getClaveIngreso() {
        return claveIngreso;
    }
}
