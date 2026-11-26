package ar.edu.utn.frsf.gestionevaluaciones.dto.excel;

import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoClaveDTO;

import java.io.Serializable;
import java.util.List;

public class ExportacionCandidatosExcelDTO implements Serializable {

    private final ArchivoExcelGeneradoDTO archivo;
    private final List<CandidatoClaveDTO> candidatos;

    public ExportacionCandidatosExcelDTO(ArchivoExcelGeneradoDTO archivo, List<CandidatoClaveDTO> candidatos) {
        this.archivo = archivo;
        this.candidatos = candidatos;
    }

    public ArchivoExcelGeneradoDTO getArchivo() {
        return archivo;
    }

    public List<CandidatoClaveDTO> getCandidatos() {
        return candidatos;
    }
}
