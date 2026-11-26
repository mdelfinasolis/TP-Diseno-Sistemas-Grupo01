package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.SesionConsultorDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

/**
 * Las pantallas del consultor (menú, CU020, CU021, CU025, CU028) solo son accesibles con la sesión de
 * consultor iniciada en CU001; si no la hay, se redirige a la pantalla inicial. Además publica el nombre
 * del consultor logueado para mostrarlo arriba a la derecha.
 */
@Component
public class InterceptorSesionConsultor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (consultor(request) != null) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + "/");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        SesionConsultorDTO consultor = consultor(request);
        // En las redirecciones no se agrega: Spring pasaría el atributo como parámetro de la URL
        boolean esRedireccion = modelAndView != null
                && (modelAndView.getView() instanceof RedirectView
                || (modelAndView.getViewName() != null && modelAndView.getViewName().startsWith("redirect:")));
        if (modelAndView != null && consultor != null && !esRedireccion) {
            modelAndView.addObject("nombreConsultor", consultor.getApellido() + ", " + consultor.getNombre());
        }
    }

    private static SesionConsultorDTO consultor(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object valor = session == null ? null : session.getAttribute(PantallaAutenticacion.SESION_CONSULTOR);
        return valor instanceof SesionConsultorDTO sesion && sesion.isValido() ? sesion : null;
    }
}
