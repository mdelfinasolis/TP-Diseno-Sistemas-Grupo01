package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registra el control de sesión del consultor sobre sus pantallas. */
@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final InterceptorSesionConsultor interceptorSesionConsultor;

    public ConfiguracionWeb(InterceptorSesionConsultor interceptorSesionConsultor) {
        this.interceptorSesionConsultor = interceptorSesionConsultor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorSesionConsultor)
                .addPathPatterns("/menu", "/preguntas/**", "/evaluar/**", "/orden-merito/**");
    }
}
