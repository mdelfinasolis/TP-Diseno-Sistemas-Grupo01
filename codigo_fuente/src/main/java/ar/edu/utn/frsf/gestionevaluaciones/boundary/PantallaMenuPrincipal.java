package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Menú principal del Consultor. No figura en el diagrama de clases: es el punto al que
 * vuelven "Salir" (CU020) y "Cancelar" (CU028) hasta que se implemente CU001.
 */
@Controller
public class PantallaMenuPrincipal {

    @Value("${app.consultor.nombre}")
    private String nombreConsultor;

    @ModelAttribute("nombreConsultor")
    public String nombreConsultor() {
        return nombreConsultor;
    }

    @GetMapping("/")
    public String mostrarMenu() {
        return "menu";
    }
}
