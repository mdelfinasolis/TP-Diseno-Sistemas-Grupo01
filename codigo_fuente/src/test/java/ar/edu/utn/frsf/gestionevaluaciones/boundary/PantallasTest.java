package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroPreguntaEntradaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.SesionConsultorDTO;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.FlashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Recorre los flujos de CU020 y CU028 a través de las pantallas (renderizando los templates). */
@SpringBootTest
@AutoConfigureMockMvc
class PantallasTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    /** Todas las solicitudes llevan la sesión de un consultor autenticado en CU001. */
    @BeforeEach
    void iniciarSesionConsultor() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto)
                .defaultRequest(get("/").sessionAttr(PantallaAutenticacion.SESION_CONSULTOR,
                        new SesionConsultorDTO("LFERRARI", "LUCIA", "FERRARI", true)))
                .build();
    }

    @Test
    void menu() throws Exception {
        mvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestionar Preguntas")))
                .andExpect(content().string(containsString("Emitir Orden de Mérito")));
    }

    @Test
    void cu020FlujoCompleto() throws Exception {
        MockHttpSession sesion = new MockHttpSession();

        // Criterios de búsqueda con la lista de competencias, sin grilla
        mvc.perform(get("/preguntas").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestión de Preguntas")))
                .andExpect(content().string(containsString("LIDERAZGO")))
                .andExpect(content().string(not(containsString("NEGOCIACIÓN"))))
                .andExpect(content().string(not(containsString("Listado de Preguntas"))));

        // Al seleccionar una competencia se cargan sus factores
        mvc.perform(get("/preguntas/factores").param("codigoCompetencia", "C02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value("F03"));

        // Buscar con el nombre en minúsculas: el servidor lo convierte a mayúsculas
        mvc.perform(post("/preguntas/buscar").session(sesion).param("nombre", "equipo"))
                .andExpect(redirectedUrl("/preguntas/grilla"));
        FiltroPreguntaEntradaDTO filtros = (FiltroPreguntaEntradaDTO) sesion.getAttribute("cu020.filtros");
        assertThat(filtros.getNombre()).isEqualTo("EQUIPO");

        mvc.perform(get("/preguntas/grilla").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Listado de Preguntas")))
                .andExpect(content().string(containsString("¿RECONOCE PÚBLICAMENTE LOS LOGROS DE SUS COMPAÑEROS?")))
                .andExpect(content().string(containsString("Mostrando 2 resultados")));

        // Modificar -> CU022 en construcción -> volver actualiza la grilla
        mvc.perform(get("/preguntas/modificar").session(sesion).param("codigo", "P004"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CU022 - Modificar Pregunta")))
                .andExpect(content().string(containsString("P004")));
        assertThat(sesion.getAttribute("cu020.preguntaSeleccionada")).isEqualTo("P004");

        // Eliminar -> CU023 en construcción
        mvc.perform(get("/preguntas/eliminar").session(sesion).param("codigo", "P005"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CU023 - Dar de baja Pregunta")));

        // Nuevo -> CU021 (PantallaAltaPregunta.abrir()); al terminar vuelve a los criterios de búsqueda
        mvc.perform(get("/preguntas/nuevo").session(sesion))
                .andExpect(redirectedUrl("/preguntas/alta"));
        mvc.perform(get("/preguntas/criterios").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Listado de Preguntas"))));

        // Salir
        mvc.perform(get("/preguntas/salir").session(sesion)).andExpect(redirectedUrl("/menu"));
        assertThat(sesion.getAttribute("cu020.filtros")).isNull();
    }

    @Test
    void cu020SinFiltrosPaginaLosResultados() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/preguntas").session(sesion));
        mvc.perform(post("/preguntas/buscar").session(sesion));

        mvc.perform(get("/preguntas/grilla").session(sesion))
                .andExpect(content().string(containsString("Mostrando 28 resultados")))
                .andExpect(content().string(containsString("Página 1 de 3")));
        mvc.perform(get("/preguntas/grilla").session(sesion).param("pagina", "3"))
                .andExpect(content().string(containsString("Página 3 de 3")));
    }

    @Test
    void cu028FlujoPrincipal() throws Exception {
        MockHttpSession sesion = new MockHttpSession();

        mvc.perform(get("/orden-merito").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Emitir Orden de Mérito")))
                .andExpect(content().string(containsString("JEFE DE PROYECTO")))
                .andExpect(content().string(containsString("AGRO LITORAL S.R.L.")))
                .andExpect(content().string(not(containsString("PUESTO DADO DE BAJA"))));

        mvc.perform(post("/orden-merito/buscar").session(sesion))
                .andExpect(redirectedUrl("/orden-merito/puestos"));
        mvc.perform(get("/orden-merito/puestos").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Búsqueda encontrada")))
                .andExpect(content().string(containsString("Mostrando 6 resultados")));

        mvc.perform(post("/orden-merito/emitir").session(sesion).param("codigoPuesto", "1515"))
                .andExpect(redirectedUrl("/orden-merito/evaluaciones"));
        mvc.perform(get("/orden-merito/evaluaciones").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PROGRAMADOR")))
                .andExpect(content().string(containsString("Cantidad de candidatos: 10")))
                .andExpect(content().string(containsString("Seleccionar todas")));

        mvc.perform(post("/orden-merito/evaluaciones").session(sesion).param("todasLasEvaluaciones", "true"))
                .andExpect(redirectedUrl("/orden-merito/listado"));
        mvc.perform(get("/orden-merito/listado").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Listado de Candidatos Evaluados")))
                .andExpect(content().string(containsString("PROGRAMADOR – FOLDER IT")))
                .andExpect(content().string(containsString("Orden de Mérito")))
                .andExpect(content().string(containsString("No alcanzan puntaje mínimo")))
                .andExpect(content().string(containsString("Cuestionarios incompletos/sin contestar")))
                .andExpect(content().string(containsString("SIN CONTESTAR")))
                .andExpect(content().string(containsString("24,00")))
                .andExpect(content().string(containsString("imprimir()")));

        mvc.perform(get("/orden-merito/cancelar").session(sesion)).andExpect(redirectedUrl("/menu"));
    }

    @Test
    void cu028EvaluacionEspecifica() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/orden-merito").session(sesion));
        mvc.perform(post("/orden-merito/buscar").session(sesion).param("codigo", "4845"));
        mvc.perform(post("/orden-merito/emitir").session(sesion).param("codigoPuesto", "4845"));
        mvc.perform(post("/orden-merito/evaluaciones").session(sesion).param("idsEvaluaciones", "3"))
                .andExpect(redirectedUrl("/orden-merito/listado"));
        mvc.perform(get("/orden-merito/listado").session(sesion))
                .andExpect(content().string(containsString("ÁLVAREZ")))
                .andExpect(content().string(containsString("RUIZ")))
                .andExpect(content().string(containsString("DÍAZ")));
    }

    @Test
    void cu028PuestoSinEvaluaciones() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/orden-merito").session(sesion));
        mvc.perform(post("/orden-merito/buscar").session(sesion).param("empresa", "agro litoral s.r.l."));

        MvcResult resultado = mvc.perform(post("/orden-merito/emitir").session(sesion).param("codigoPuesto", "7845"))
                .andExpect(redirectedUrl("/orden-merito/puestos"))
                .andReturn();
        FlashMap flash = resultado.getFlashMap();
        assertThat(flash.get("mensaje")).isEqualTo("El puesto seleccionado no posee evaluaciones.");

        mvc.perform(get("/orden-merito/puestos").session(sesion).flashAttrs(flash))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El puesto seleccionado no posee evaluaciones.")))
                .andExpect(content().string(containsString("SOPORTE TÉCNICO")));
    }
}
