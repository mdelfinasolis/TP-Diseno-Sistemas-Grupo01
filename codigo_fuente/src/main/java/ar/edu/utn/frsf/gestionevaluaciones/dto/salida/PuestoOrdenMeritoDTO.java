package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

public class PuestoOrdenMeritoDTO implements Serializable {

    private final String codigo;
    private final String nombre;
    private final String empresa;
    private final int cantidadCandidatos;
    private final int evaluacionesCompletas;

    public PuestoOrdenMeritoDTO(String codigo, String nombre, String empresa,
                                int cantidadCandidatos, int evaluacionesCompletas) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.empresa = empresa;
        this.cantidadCandidatos = cantidadCandidatos;
        this.evaluacionesCompletas = evaluacionesCompletas;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmpresa() {
        return empresa;
    }

    public int getCantidadCandidatos() {
        return cantidadCandidatos;
    }

    public int getEvaluacionesCompletas() {
        return evaluacionesCompletas;
    }
}
