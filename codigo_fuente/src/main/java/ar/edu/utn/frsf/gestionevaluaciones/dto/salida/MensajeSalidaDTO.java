package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;
import java.util.List;

/** Resultado de una operación de alta: tipo EXITO o ERROR, con la lista de errores si corresponde. */
public class MensajeSalidaDTO implements Serializable {

    public static final String EXITO = "EXITO";
    public static final String ERROR = "ERROR";

    private final String tipo;
    private final String titulo;
    private final String descripcion;
    private final List<String> errores;

    public MensajeSalidaDTO(String tipo, String titulo, String descripcion, List<String> errores) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.errores = errores != null ? List.copyOf(errores) : List.of();
    }

    public String getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<String> getErrores() {
        return errores;
    }
}
