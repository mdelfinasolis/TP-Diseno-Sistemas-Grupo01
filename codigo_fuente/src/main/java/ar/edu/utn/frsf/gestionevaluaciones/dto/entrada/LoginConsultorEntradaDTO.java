package ar.edu.utn.frsf.gestionevaluaciones.dto.entrada;

import java.io.Serializable;

public class LoginConsultorEntradaDTO implements Serializable {

    private String usuario;
    private String contrasena;

    public LoginConsultorEntradaDTO() {
    }

    public LoginConsultorEntradaDTO(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
