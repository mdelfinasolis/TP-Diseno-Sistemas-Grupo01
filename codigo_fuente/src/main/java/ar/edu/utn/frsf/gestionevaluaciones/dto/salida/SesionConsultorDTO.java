package ar.edu.utn.frsf.gestionevaluaciones.dto.salida;

import java.io.Serializable;

/** Resultado de autenticar a un consultor (CU001). Se guarda en la sesión mientras está logueado. */
public class SesionConsultorDTO implements Serializable {

    private final String nombreUsuario;
    private final String nombre;
    private final String apellido;
    private final boolean valido;

    public SesionConsultorDTO(String nombreUsuario, String nombre, String apellido, boolean valido) {
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.valido = valido;
    }

    /** CU001 5.A: el usuario o la contraseña no son válidos. */
    public static SesionConsultorDTO invalida() {
        return new SesionConsultorDTO(null, null, null, false);
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public boolean isValido() {
        return valido;
    }
}
