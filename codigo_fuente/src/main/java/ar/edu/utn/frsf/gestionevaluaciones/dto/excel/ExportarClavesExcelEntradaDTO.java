package ar.edu.utn.frsf.gestionevaluaciones.dto.excel;

import java.io.Serializable;

/**
 * Datos para exportar candidatos y claves. Como la exportación se descarga desde el navegador,
 * rutaDestino no se usa (la carpeta la elige el navegador del Consultor).
 */
public class ExportarClavesExcelEntradaDTO implements Serializable {

    private int idEvaluacion;
    private String rutaDestino;
    private String nombreArchivo;

    public ExportarClavesExcelEntradaDTO() {
    }

    public ExportarClavesExcelEntradaDTO(int idEvaluacion, String rutaDestino, String nombreArchivo) {
        this.idEvaluacion = idEvaluacion;
        this.rutaDestino = rutaDestino;
        this.nombreArchivo = nombreArchivo;
    }

    public int getIdEvaluacion() {
        return idEvaluacion;
    }

    public void setIdEvaluacion(int idEvaluacion) {
        this.idEvaluacion = idEvaluacion;
    }

    public String getRutaDestino() {
        return rutaDestino;
    }

    public void setRutaDestino(String rutaDestino) {
        this.rutaDestino = rutaDestino;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
}
