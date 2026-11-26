package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

public class FiltroPuestoEntradaDTO implements Serializable {

    private String codigo;
    private String nombre;
    private String empresa;

    public FiltroPuestoEntradaDTO() {
    }

    public FiltroPuestoEntradaDTO(String codigo, String nombre, String empresa) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.empresa = empresa;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
