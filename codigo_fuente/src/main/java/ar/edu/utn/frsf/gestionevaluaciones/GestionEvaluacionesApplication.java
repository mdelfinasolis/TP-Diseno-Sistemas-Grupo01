package ar.edu.utn.frsf.gestionevaluaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Punto de entrada. Extiende SpringBootServletInitializer para poder desplegarse
 * como gestion-evaluaciones.war en Tomcat 10 (diagrama de despliegue), y también
 * puede ejecutarse directamente con mvnw spring-boot:run.
 */
@SpringBootApplication
public class GestionEvaluacionesApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GestionEvaluacionesApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(GestionEvaluacionesApplication.class, args);
    }
}
