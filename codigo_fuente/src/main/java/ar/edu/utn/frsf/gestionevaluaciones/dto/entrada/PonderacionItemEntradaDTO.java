package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

public class PonderacionItemEntradaDTO implements Serializable {

    private int ordenItem;
    private double ponderacion;

    public PonderacionItemEntradaDTO() {
    }

    public PonderacionItemEntradaDTO(int ordenItem, double ponderacion) {
        this.ordenItem = ordenItem;
        this.ponderacion = ponderacion;
    }

    public int getOrdenItem() {
        return ordenItem;
    }

    public void setOrdenItem(int ordenItem) {
        this.ordenItem = ordenItem;
    }

    public double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(double ponderacion) {
        this.ponderacion = ponderacion;
    }
}
