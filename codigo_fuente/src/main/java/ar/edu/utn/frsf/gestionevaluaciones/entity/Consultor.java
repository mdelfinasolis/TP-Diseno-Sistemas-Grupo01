package ar.edu.utn.frsf.gestionevaluaciones.entity;

/**
 * Consultor (diagrama de clases). Sus datos provienen del LDAP de la organización al autenticarse (CU001):
 * no tiene DAO ni tabla propia, y la contraseña no se guarda en el sistema.
 */
public class Consultor extends Usuario {

    private final String nombreUsuario;

    public Consultor(String nombreUsuario, String nombre, String apellido) {
        super(nombre, apellido);
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}
