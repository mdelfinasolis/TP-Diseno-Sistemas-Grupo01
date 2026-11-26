package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Pantalla principal del Consultor (CU001, paso 6): opciones del perfil Consultor.
 * Solo accesible con sesión de consultor iniciada (InterceptorSesionConsultor).
 */
@Controller
public class PantallaMenuPrincipal {

    @GetMapping("/menu")
    public String mostrarMenu() {
        return "menu";
    }
}
