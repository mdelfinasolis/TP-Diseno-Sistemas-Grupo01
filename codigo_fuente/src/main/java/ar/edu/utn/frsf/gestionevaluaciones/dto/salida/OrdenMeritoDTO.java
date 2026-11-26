package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Orden de mérito emitido. La lista de candidatos viene ordenada:
 * primero COMPLETO que cumplen todos los mínimos (por puntaje descendente),
 * luego COMPLETO que no cumplen algún mínimo, y por último el resto agrupado por estado.
 */
public class OrdenMeritoDTO implements Serializable {

    private final String codigoPuesto;
    private final String nombrePuesto;
    private final String empresa;
    private final LocalDateTime fechaEmision;
    private final List<CandidatoOrdenMeritoDTO> candidatos;

    public OrdenMeritoDTO(String codigoPuesto, String nombrePuesto, String empresa,
                          LocalDateTime fechaEmision, List<CandidatoOrdenMeritoDTO> candidatos) {
        this.codigoPuesto = codigoPuesto;
        this.nombrePuesto = nombrePuesto;
        this.empresa = empresa;
        this.fechaEmision = fechaEmision;
        this.candidatos = candidatos;
    }

    public String getCodigoPuesto() {
        return codigoPuesto;
    }

    public String getNombrePuesto() {
        return nombrePuesto;
    }

    public String getEmpresa() {
        return empresa;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public List<CandidatoOrdenMeritoDTO> getCandidatos() {
        return candidatos;
    }
}
