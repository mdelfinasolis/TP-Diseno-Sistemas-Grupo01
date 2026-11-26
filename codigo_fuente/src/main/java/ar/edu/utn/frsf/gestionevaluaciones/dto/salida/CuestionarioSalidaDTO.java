package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;

import java.io.Serializable;

/**
 * Estado del cuestionario devuelto al ingresar (CU026) o al verificar su vigencia.
 * valido = false cuando no existe la clave, el documento no coincide o el cuestionario
 * no está ACTIVO ni EN_PROCESO. segundosRestantes alimenta la cuenta regresiva (0 si venció el tiempo).
 */
public class CuestionarioSalidaDTO implements Serializable {

    private final String nombreCandidato;
    private final String nombrePuesto;
    private final ESTADOCUESTIONARIO estado;
    private final int bloqueActual;
    private final int tiempoMaximo;
    private final int segundosRestantes;
    private final boolean valido;

    public CuestionarioSalidaDTO(String nombreCandidato, String nombrePuesto, ESTADOCUESTIONARIO estado,
                                 int bloqueActual, int tiempoMaximo, int segundosRestantes, boolean valido) {
        this.nombreCandidato = nombreCandidato;
        this.nombrePuesto = nombrePuesto;
        this.estado = estado;
        this.bloqueActual = bloqueActual;
        this.tiempoMaximo = tiempoMaximo;
        this.segundosRestantes = segundosRestantes;
        this.valido = valido;
    }

    /** Resultado del ingreso cuando los datos no son válidos (CU001 3.A.2.1). */
    public static CuestionarioSalidaDTO invalido() {
        return new CuestionarioSalidaDTO(null, null, null, 0, 0, 0, false);
    }

    public String getNombreCandidato() {
        return nombreCandidato;
    }

    public String getNombrePuesto() {
        return nombrePuesto;
    }

    public ESTADOCUESTIONARIO getEstado() {
        return estado;
    }

    public int getBloqueActual() {
        return bloqueActual;
    }

    public int getTiempoMaximo() {
        return tiempoMaximo;
    }

    public int getSegundosRestantes() {
        return segundosRestantes;
    }

    public boolean isValido() {
        return valido;
    }
}
