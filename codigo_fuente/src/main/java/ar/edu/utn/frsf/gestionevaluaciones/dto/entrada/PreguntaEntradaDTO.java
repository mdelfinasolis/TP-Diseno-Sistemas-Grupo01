package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PreguntaEntradaDTO implements Serializable {

    private String codigo;
    private String nombre;
    private String enunciado;
    private String descripcion;
    private String codigoFactor;
    private String nombreOpcionRespuesta;
    private List<PonderacionItemEntradaDTO> ponderaciones = new ArrayList<>();

    public PreguntaEntradaDTO() {
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

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigoFactor() {
        return codigoFactor;
    }

    public void setCodigoFactor(String codigoFactor) {
        this.codigoFactor = codigoFactor;
    }

    public String getNombreOpcionRespuesta() {
        return nombreOpcionRespuesta;
    }

    public void setNombreOpcionRespuesta(String nombreOpcionRespuesta) {
        this.nombreOpcionRespuesta = nombreOpcionRespuesta;
    }

    public List<PonderacionItemEntradaDTO> getPonderaciones() {
        return ponderaciones;
    }

    public void setPonderaciones(List<PonderacionItemEntradaDTO> ponderaciones) {
        this.ponderaciones = ponderaciones != null ? ponderaciones : new ArrayList<>();
    }
}
