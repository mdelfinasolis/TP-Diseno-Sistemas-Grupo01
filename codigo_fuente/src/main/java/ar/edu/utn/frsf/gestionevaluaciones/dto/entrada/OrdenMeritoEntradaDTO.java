package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrdenMeritoEntradaDTO implements Serializable {

    private String codigoPuesto;
    private List<Integer> idsEvaluaciones = new ArrayList<>();
    private boolean todasLasEvaluaciones;

    public OrdenMeritoEntradaDTO() {
    }

    public OrdenMeritoEntradaDTO(String codigoPuesto, List<Integer> idsEvaluaciones, boolean todasLasEvaluaciones) {
        this.codigoPuesto = codigoPuesto;
        this.idsEvaluaciones = idsEvaluaciones != null ? idsEvaluaciones : new ArrayList<>();
        this.todasLasEvaluaciones = todasLasEvaluaciones;
    }

    public String getCodigoPuesto() {
        return codigoPuesto;
    }

    public void setCodigoPuesto(String codigoPuesto) {
        this.codigoPuesto = codigoPuesto;
    }

    public List<Integer> getIdsEvaluaciones() {
        return idsEvaluaciones;
    }

    public void setIdsEvaluaciones(List<Integer> idsEvaluaciones) {
        this.idsEvaluaciones = idsEvaluaciones != null ? idsEvaluaciones : new ArrayList<>();
    }

    public boolean isTodasLasEvaluaciones() {
        return todasLasEvaluaciones;
    }

    public void setTodasLasEvaluaciones(boolean todasLasEvaluaciones) {
        this.todasLasEvaluaciones = todasLasEvaluaciones;
    }
}
