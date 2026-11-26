package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoClaveDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EvaluarCandidatosEntradaDTO implements Serializable {

    private String codigoPuesto;
    private List<CandidatoClaveDTO> candidatos = new ArrayList<>();
    private int cantPreguntasPorBloque;

    public EvaluarCandidatosEntradaDTO() {
    }

    public EvaluarCandidatosEntradaDTO(String codigoPuesto, List<CandidatoClaveDTO> candidatos, int cantPreguntasPorBloque) {
        this.codigoPuesto = codigoPuesto;
        this.candidatos = candidatos != null ? candidatos : new ArrayList<>();
        this.cantPreguntasPorBloque = cantPreguntasPorBloque;
    }

    public String getCodigoPuesto() {
        return codigoPuesto;
    }

    public void setCodigoPuesto(String codigoPuesto) {
        this.codigoPuesto = codigoPuesto;
    }

    public List<CandidatoClaveDTO> getCandidatos() {
        return candidatos;
    }

    public void setCandidatos(List<CandidatoClaveDTO> candidatos) {
        this.candidatos = candidatos != null ? candidatos : new ArrayList<>();
    }

    public int getCantPreguntasPorBloque() {
        return cantPreguntasPorBloque;
    }

    public void setCantPreguntasPorBloque(int cantPreguntasPorBloque) {
        this.cantPreguntasPorBloque = cantPreguntasPorBloque;
    }
}
