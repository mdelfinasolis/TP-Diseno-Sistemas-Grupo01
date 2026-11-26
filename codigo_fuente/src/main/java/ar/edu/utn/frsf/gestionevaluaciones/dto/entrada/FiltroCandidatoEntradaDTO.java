package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

/** Filtros de CU025. nroCandidato es Integer para que el filtro vacío no se aplique. */
public class FiltroCandidatoEntradaDTO implements Serializable {

    private String apellido;
    private String nombre;
    private Integer nroCandidato;

    public FiltroCandidatoEntradaDTO() {
    }

    public FiltroCandidatoEntradaDTO(String apellido, String nombre, Integer nroCandidato) {
        this.apellido = apellido;
        this.nombre = nombre;
        this.nroCandidato = nroCandidato;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getNroCandidato() {
        return nroCandidato;
    }

    public void setNroCandidato(Integer nroCandidato) {
        this.nroCandidato = nroCandidato;
    }
}
