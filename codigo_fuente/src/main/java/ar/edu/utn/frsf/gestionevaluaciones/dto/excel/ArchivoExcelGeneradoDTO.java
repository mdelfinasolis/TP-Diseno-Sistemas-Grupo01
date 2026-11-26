package ar.edu.utn.frsf.gestionevaluaciones.dto.excel;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ArchivoExcelGeneradoDTO implements Serializable {

    private final String nombreArchivo;
    private final String rutaDestino;
    private final LocalDateTime fechaGeneracion;
    private final int cantidadRegistros;
    private final String mensaje;

    public ArchivoExcelGeneradoDTO(String nombreArchivo, String rutaDestino, LocalDateTime fechaGeneracion,
                                   int cantidadRegistros, String mensaje) {
        this.nombreArchivo = nombreArchivo;
        this.rutaDestino = rutaDestino;
        this.fechaGeneracion = fechaGeneracion;
        this.cantidadRegistros = cantidadRegistros;
        this.mensaje = mensaje;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getRutaDestino() {
        return rutaDestino;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public int getCantidadRegistros() {
        return cantidadRegistros;
    }

    public String getMensaje() {
        return mensaje;
    }
}
