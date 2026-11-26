package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.control.GestorCuestionarios;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.ObtenerBloqueCuestionarioEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PreguntaCuestionarioDTO;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Recorre CU021, CU025 y CU026 a través de las pantallas. Cada test se revierte al terminar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PantallasEtapa8Test {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private GestorCuestionarios gestorCuestionarios;

    // =====================================================================
    // CU021
    // =====================================================================

    @Test
    void cu021ErroresYAltaExitosa() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/preguntas/alta").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dar de Alta Pregunta")))
                .andExpect(content().string(containsString("Generar con IA")));

        mvc.perform(get("/preguntas/alta/opcion").param("nombre", "FRECUENCIA"))
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.items[0].descripcion").value("NUNCA"));

        // 2.A: se detallan todas las omisiones, con alerta auditiva y sin tapar campos
        mvc.perform(post("/preguntas/alta").session(sesion).param("nombre", "solo nombre"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La pregunta es obligatoria.")))
                .andExpect(content().string(containsString("Debe seleccionar la opción de respuesta.")))
                .andExpect(content().string(containsString("data-alerta-auditiva=\"true\"")))
                .andExpect(content().string(containsString("value=\"SOLO NOMBRE\"")));

        // Alta exitosa: los literales se guardan en mayúsculas
        mvc.perform(conPonderaciones(post("/preguntas/alta").session(sesion)
                        .param("codigoCompetencia", "C01").param("codigoFactor", "F01")
                        .param("nombre", "liderazgo situacional")
                        .param("enunciado", "¿adapta su estilo de liderazgo?")
                        .param("nombreOpcionRespuesta", "FRECUENCIA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "La Pregunta LIDERAZGO SITUACIONAL se ha creado correctamente. ¿Desea cargar otra?")));

        mvc.perform(get("/preguntas/alta/no").session(sesion)).andExpect(redirectedUrl("/preguntas/criterios"));
        mvc.perform(get("/preguntas/alta/si").session(sesion)).andExpect(redirectedUrl("/preguntas/alta"));

        // La pregunta aparece en CU020
        mvc.perform(post("/preguntas/buscar").session(sesion).param("nombre", "situacional"));
        mvc.perform(get("/preguntas/grilla").session(sesion))
                .andExpect(content().string(containsString("¿ADAPTA SU ESTILO DE LIDERAZGO?")));
    }

    @Test
    void cu021PreguntaExistenteYGenerarConIA() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(conPonderaciones(post("/preguntas/alta").session(sesion)
                        .param("codigoCompetencia", "C01").param("codigoFactor", "F01")
                        .param("nombre", "DECISIONES BAJO PRESIÓN").param("enunciado", "¿TEXTO NUEVO?")
                        .param("nombreOpcionRespuesta", "FRECUENCIA")))
                .andExpect(content().string(containsString("La pregunta ya existe en el factor")));

        // Generar con IA -> CU033 en construcción; al volver se conservan los datos
        mvc.perform(post("/preguntas/alta/ia").session(sesion).param("nombre", "pregunta en curso"))
                .andExpect(content().string(containsString("CU033 - Generar pregunta con Inteligencia Artificial")));
        mvc.perform(get("/preguntas/alta").session(sesion))
                .andExpect(content().string(containsString("value=\"PREGUNTA EN CURSO\"")));
        mvc.perform(get("/preguntas/alta/cancelar").session(sesion)).andExpect(redirectedUrl("/preguntas/criterios"));
    }

    private static MockHttpServletRequestBuilder conPonderaciones(MockHttpServletRequestBuilder request) {
        for (int i = 0; i < 5; i++) {
            request.param("ponderaciones[" + i + "].ordenItem", String.valueOf(i + 1))
                    .param("ponderaciones[" + i + "].ponderacion", String.valueOf(i));
        }
        return request;
    }

    // =====================================================================
    // CU025
    // =====================================================================

    @Test
    void cu025FlujoCompletoConExportacion() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/evaluar").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Evaluar Candidatos")));

        mvc.perform(post("/evaluar/buscar").session(sesion)).andExpect(redirectedUrl("/evaluar/candidatos"));
        mvc.perform(get("/evaluar/candidatos").session(sesion))
                .andExpect(content().string(containsString("Candidatos encontrados")))
                .andExpect(content().string(containsString("Mostrando 25 resultados")));

        // 4.A: Agregar sin seleccionar
        mvc.perform(post("/evaluar/agregar").session(sesion))
                .andExpect(flash().attribute("mensajeError", "Se debe seleccionar al menos un candidato."));

        // 6.A: ROMERO (8) tiene un cuestionario ACTIVO
        mvc.perform(post("/evaluar/agregar").session(sesion).param("nrosCandidatos", "16", "17", "8"));
        mvc.perform(post("/evaluar/siguiente").session(sesion))
                .andExpect(redirectedUrl("/evaluar/candidatos"))
                .andExpect(flash().attributeExists("noEvaluables"));
        mvc.perform(post("/evaluar/quitar").session(sesion).param("nrosCandidatos", "8"));
        mvc.perform(post("/evaluar/siguiente").session(sesion)).andExpect(redirectedUrl("/evaluar/puesto"));

        // 10.A: Siguiente sin función o puesto
        mvc.perform(post("/evaluar/puesto/siguiente").session(sesion))
                .andExpect(flash().attribute("mensajeError", "Se debe seleccionar al menos una función o puesto."));

        // 9.A: DISEÑADOR UX requiere una competencia no evaluable
        mvc.perform(post("/evaluar/puesto").session(sesion).param("codigoPuesto", "3030"))
                .andExpect(flash().attribute("competenciasNoEvaluables", List.of("CREATIVIDAD")));

        mvc.perform(post("/evaluar/puesto").session(sesion).param("codigoPuesto", "6060"));
        mvc.perform(get("/evaluar/puesto").session(sesion))
                .andExpect(content().string(containsString("ANALISTA DE DATOS")))
                .andExpect(content().string(containsString("ADAPTABILIDAD")));
        mvc.perform(post("/evaluar/puesto/siguiente").session(sesion).param("codigoPuesto", "6060"))
                .andExpect(redirectedUrl("/evaluar/claves"));
        mvc.perform(get("/evaluar/claves").session(sesion))
                .andExpect(content().string(containsString("Clave de Ingreso")))
                .andExpect(content().string(containsString("GARCÍA")))
                .andExpect(content().string(not(containsString("Información guardada con éxito."))));

        mvc.perform(post("/evaluar/finalizar").session(sesion)).andExpect(redirectedUrl("/evaluar/claves"));
        mvc.perform(get("/evaluar/claves").session(sesion))
                .andExpect(content().string(containsString("Información guardada con éxito.")));

        // 16.A: Cancelar en el diálogo vuelve a la pregunta de exportación
        mvc.perform(get("/evaluar/exportar").session(sesion))
                .andExpect(content().string(containsString("-ANALISTA DE DATOS.xls")));
        mvc.perform(get("/evaluar/cancelar").session(sesion).param("paso", "exportacion"))
                .andExpect(redirectedUrl("/evaluar/claves"));

        MvcResult descarga = mvc.perform(post("/evaluar/exportar").session(sesion)
                        .param("nombreArchivo", "2026-11-26-10-30-analista de datos.xls"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.ms-excel"))
                .andExpect(header().string("Content-Disposition", containsString("ANALISTA%20DE%20DATOS.xls")))
                .andReturn();
        try (HSSFWorkbook libro = new HSSFWorkbook(new ByteArrayInputStream(descarga.getResponse().getContentAsByteArray()))) {
            assertThat(libro.getSheetAt(0).getRow(0).getCell(4).getStringCellValue()).isEqualTo("Clave de Ingreso");
            assertThat(libro.getSheetAt(0).getPhysicalNumberOfRows()).isEqualTo(3);
        }

        mvc.perform(get("/evaluar/cancelar").session(sesion)).andExpect(redirectedUrl("/"));
    }

    // =====================================================================
    // CU026
    // =====================================================================

    private MockHttpServletRequestBuilder ingreso(MockHttpSession sesion, String nroDocumento, String clave) {
        return post("/cuestionario").session(sesion)
                .param("tipoDocumento", "DNI").param("nroDocumento", nroDocumento).param("claveIngreso", clave);
    }

    @Test
    void cu026DatosInvalidosYVencimientos() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/cuestionario").session(sesion))
                .andExpect(content().string(containsString("type=\"password\"")));

        mvc.perform(ingreso(sesion, "42111001", "MALA1234"))
                .andExpect(content().string(containsString(
                        "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato")))
                .andExpect(content().string(not(containsString("value=\"42111001\""))));

        mvc.perform(ingreso(sesion, "42111002", "VENCID38"))
                .andExpect(content().string(containsString("Tiempo de espera alcanzado.")))
                .andExpect(content().string(containsString("SIN CONTESTAR")));
        mvc.perform(ingreso(sesion, "42111004", "TIEMPO52"))
                .andExpect(content().string(containsString("Tiempo máximo alcanzado.")))
                .andExpect(content().string(containsString("INCOMPLETO")));
        mvc.perform(ingreso(sesion, "42111005", "ACCESO63"))
                .andExpect(content().string(containsString("Cantidad máxima de accesos alcanzada.")));

        // 1.C: EN_PROCESO retoma en el bloque pendiente
        mvc.perform(ingreso(sesion, "42111003", "proces49")).andExpect(redirectedUrl("/cuestionario/bloque"));
        mvc.perform(get("/cuestionario/bloque").session(sesion))
                .andExpect(content().string(containsString("Bloque (2/2)")))
                .andExpect(content().string(containsString("Finalizar")))
                .andExpect(content().string(containsString("RÍOS, CAROLINA")));
    }

    @Test
    void cu026FlujoCompleto() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(ingreso(sesion, "42111001", "activa27")).andExpect(redirectedUrl("/cuestionario/instrucciones"));
        mvc.perform(get("/cuestionario/instrucciones").session(sesion))
                .andExpect(content().string(containsString("Lea atentamente las instrucciones antes de comenzar")))
                .andExpect(content().string(containsString("00:20:00")))
                .andExpect(content().string(containsString("VEGA, LAURA")));

        mvc.perform(post("/cuestionario/aceptar").session(sesion)).andExpect(redirectedUrl("/cuestionario/bloque"));
        mvc.perform(get("/cuestionario/bloque").session(sesion))
                .andExpect(content().string(containsString("Bloque (1/2)")))
                .andExpect(content().string(containsString("Siguiente")));

        // 6.A: faltan respuestas -> mismo bloque, con alerta auditiva
        List<PreguntaCuestionarioDTO> bloque1 = preguntas(1);
        mvc.perform(post("/cuestionario/siguiente").session(sesion)
                        .param("respuesta_" + bloque1.get(1).getCodigoPregunta(), "3"))
                .andExpect(content().string(containsString("Las siguientes preguntas no han sido respondidas:")))
                .andExpect(content().string(containsString("Pregunta 1")))
                .andExpect(content().string(not(containsString("<li>Pregunta 2</li>"))))
                .andExpect(content().string(containsString("Bloque (1/2)")))
                .andExpect(content().string(containsString("data-alerta-auditiva=\"true\"")));

        mvc.perform(responder(post("/cuestionario/siguiente").session(sesion), bloque1))
                .andExpect(redirectedUrl("/cuestionario/bloque"));
        mvc.perform(get("/cuestionario/bloque").session(sesion))
                .andExpect(content().string(containsString("Bloque (2/2)")));

        mvc.perform(responder(post("/cuestionario/finalizar").session(sesion), preguntas(2)))
                .andExpect(content().string(containsString("El cuestionario ha sido completado con éxito.")))
                .andExpect(content().string(containsString("COMPLETO")));

        // Ya completo: no puede volver a ingresar
        mvc.perform(ingreso(sesion, "42111001", "ACTIVA27"))
                .andExpect(content().string(containsString("Los datos ingresados no son válidos")));
    }

    @Test
    void cu026FinalizarDesdeUnBloqueQueNoEsElUltimoNoCompletaElCuestionario() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(ingreso(sesion, "42111001", "ACTIVA27"));
        mvc.perform(post("/cuestionario/aceptar").session(sesion));

        // Un POST a Finalizar con el bloque 1 respondido solo guarda ese bloque y pasa al 2
        mvc.perform(responder(post("/cuestionario/finalizar").session(sesion), preguntas(1)))
                .andExpect(redirectedUrl("/cuestionario/bloque"));
        mvc.perform(get("/cuestionario/bloque").session(sesion))
                .andExpect(content().string(containsString("Bloque (2/2)")));
        assertThat(gestorCuestionarios.obtenerPorClave("ACTIVA27").getDetalle().getEstado().name())
                .isEqualTo("EN_PROCESO");
    }

    private List<PreguntaCuestionarioDTO> preguntas(int nroBloque) {
        return gestorCuestionarios.listarPreguntasBloque(new ObtenerBloqueCuestionarioEntradaDTO("ACTIVA27", nroBloque));
    }

    private static MockHttpServletRequestBuilder responder(MockHttpServletRequestBuilder request,
                                                           List<PreguntaCuestionarioDTO> preguntas) {
        preguntas.forEach(p -> request.param("respuesta_" + p.getCodigoPregunta(), "4"));
        return request;
    }
}
