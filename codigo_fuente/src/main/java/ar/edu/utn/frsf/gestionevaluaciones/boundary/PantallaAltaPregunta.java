package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.control.GestorCompetencias;
import ar.edu.utn.frsf.gestionevaluaciones.control.GestorFactores;
import ar.edu.utn.frsf.gestionevaluaciones.control.GestorOpcionesRespuesta;
import ar.edu.utn.frsf.gestionevaluaciones.control.GestorPreguntas;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFactorEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.PonderacionItemEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.PreguntaEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.FactorListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.MensajeSalidaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.OpcionRespuestaDetalleDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CU021 - Dar de alta Pregunta. La abre PantallaGestionPreguntas.nuevo() (CU020).
 * Errores de validación: se muestran en un panel sin tapar campos ni botones (enunciado, 2.A.1 y 2.B.1),
 * conservando los datos ingresados. "Generar con IA" lleva a CU033 (en construcción) y al volver
 * se recuperan los datos cargados.
 */
@Controller
@RequestMapping("/preguntas/alta")
public class PantallaAltaPregunta {

    private static final String SESION_DATOS = "cu021.datos";
    private static final String SESION_COMPETENCIA = "cu021.codigoCompetencia";

    private final GestorPreguntas gestorPreguntas;
    private final GestorCompetencias gestorCompetencias;
    private final GestorFactores gestorFactores;
    private final GestorOpcionesRespuesta gestorOpcionesRespuesta;

    @Value("${app.consultor.nombre}")
    private String nombreConsultor;

    public PantallaAltaPregunta(GestorPreguntas gestorPreguntas, GestorCompetencias gestorCompetencias,
                                GestorFactores gestorFactores, GestorOpcionesRespuesta gestorOpcionesRespuesta) {
        this.gestorPreguntas = gestorPreguntas;
        this.gestorCompetencias = gestorCompetencias;
        this.gestorFactores = gestorFactores;
        this.gestorOpcionesRespuesta = gestorOpcionesRespuesta;
    }

    /** Los datos literales se ingresan siempre en mayúsculas: se normalizan en el servidor. */
    @InitBinder
    public void convertirAMayusculas(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String texto) {
                setValue(texto == null || texto.isBlank() ? null : texto.trim().toUpperCase(Locale.ROOT));
            }
        });
    }

    @ModelAttribute("nombreConsultor")
    public String nombreConsultor() {
        return nombreConsultor;
    }

    /** Paso 1: pantalla de alta con las listas de competencias y opciones de respuesta. */
    @GetMapping
    public String abrir(Model model, HttpSession session) {
        PreguntaEntradaDTO datos = (PreguntaEntradaDTO) session.getAttribute(SESION_DATOS);
        String codigoCompetencia = (String) session.getAttribute(SESION_COMPETENCIA);
        cargarPantalla(model, datos != null ? datos : new PreguntaEntradaDTO(), codigoCompetencia);
        return "altaPregunta";
    }

    /** Al seleccionar una competencia se cargan sus factores. */
    @GetMapping("/factores")
    @ResponseBody
    public List<FactorListadoDTO> seleccionarCompetencia(@RequestParam(required = false) String codigoCompetencia) {
        if (codigoCompetencia == null || codigoCompetencia.isBlank()) {
            return List.of();
        }
        return gestorFactores.buscarFactores(new FiltroFactorEntradaDTO(codigoCompetencia, ""));
    }

    /** Al seleccionar la opción de respuesta se muestra la grilla de ítems (orden, descripción, ponderación). */
    @GetMapping("/opcion")
    @ResponseBody
    public ResponseEntity<OpcionRespuestaDetalleDTO> seleccionarOpcionRespuesta(
            @RequestParam(name = "nombre", required = false) String nombreOpcion) {
        if (nombreOpcion == null || nombreOpcion.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        OpcionRespuestaDetalleDTO detalle = gestorOpcionesRespuesta.obtenerDetalleOpcion(nombreOpcion);
        return detalle == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(detalle);
    }

    /** Botón Aceptar: crearPregunta valida y registra; ante errores se conservan los datos. */
    @PostMapping
    public String aceptar(@ModelAttribute("pregunta") PreguntaEntradaDTO datos,
                          @RequestParam(required = false) String codigoCompetencia,
                          Model model, HttpSession session) {
        MensajeSalidaDTO mensaje = gestorPreguntas.crearPregunta(datos);

        if (MensajeSalidaDTO.ERROR.equals(mensaje.getTipo())) {
            mostrarErrores(mensaje.getErrores(), model);
            emitirAlertaAuditiva(model);
            cargarPantalla(model, datos, codigoCompetencia);
            return "altaPregunta";
        }

        limpiarFormulario(session);
        cargarPantalla(model, datos, codigoCompetencia);
        model.addAttribute("mensajeExito", mensaje.getDescripcion());
        return "altaPregunta";
    }

    /** Botón "Generar con IA": invoca CU033 (en construcción) guardando los datos ya ingresados. */
    @PostMapping("/ia")
    public String generarConIA(@ModelAttribute("pregunta") PreguntaEntradaDTO datos,
                               @RequestParam(required = false) String codigoCompetencia,
                               Model model, HttpSession session) {
        session.setAttribute(SESION_DATOS, datos);
        session.setAttribute(SESION_COMPETENCIA, codigoCompetencia);
        model.addAttribute("tituloPantalla", "Generar pregunta con IA");
        model.addAttribute("casoDeUso", "CU033 - Generar pregunta con Inteligencia Artificial");
        model.addAttribute("urlVolver", "/preguntas/alta");
        return "enConstruccion";
    }

    /** Muestra el panel con TODOS los errores de validación. */
    public void mostrarErrores(List<String> errores, Model model) {
        model.addAttribute("errores", errores);
    }

    /** La alerta auditiva la emite el navegador (pantallas.js, emitirAlertaAuditiva()). */
    public void emitirAlertaAuditiva(Model model) {
        model.addAttribute("alertaAuditiva", true);
    }

    public void limpiarFormulario(HttpSession session) {
        session.removeAttribute(SESION_DATOS);
        session.removeAttribute(SESION_COMPETENCIA);
    }

    /** "¿Desea cargar otra?" - SI: pantalla de alta vacía. */
    @GetMapping("/si")
    public String seleccionarSI(HttpSession session) {
        limpiarFormulario(session);
        return "redirect:/preguntas/alta";
    }

    /** "¿Desea cargar otra?" - NO: termina CU021 y vuelve a CU020. */
    @GetMapping("/no")
    public String seleccionarNO(HttpSession session) {
        limpiarFormulario(session);
        return "redirect:/preguntas/criterios";
    }

    /** Botón Cancelar: termina CU021 y vuelve a CU020. */
    @GetMapping("/cancelar")
    public String cancelar(HttpSession session) {
        limpiarFormulario(session);
        return "redirect:/preguntas/criterios";
    }

    /** Botón Salir: sale sin guardar y vuelve a CU020. */
    @GetMapping("/salir")
    public String salir(HttpSession session) {
        limpiarFormulario(session);
        return "redirect:/preguntas/criterios";
    }

    private void cargarPantalla(Model model, PreguntaEntradaDTO datos, String codigoCompetencia) {
        model.addAttribute("pregunta", datos);
        model.addAttribute("codigoCompetencia", codigoCompetencia);
        model.addAttribute("competencias", gestorCompetencias.listarCompetencias());
        model.addAttribute("factores", seleccionarCompetencia(codigoCompetencia));
        model.addAttribute("opciones", gestorOpcionesRespuesta.listarOpciones());

        OpcionRespuestaDetalleDTO detalle = seleccionarOpcionRespuesta(datos.getNombreOpcionRespuesta()).getBody();
        model.addAttribute("opcionDetalle", detalle);
        Map<Integer, Integer> ponderacionesPorOrden = new HashMap<>();
        for (PonderacionItemEntradaDTO p : datos.getPonderaciones()) {
            ponderacionesPorOrden.put(p.getOrdenItem(), (int) Math.round(p.getPonderacion()));
        }
        model.addAttribute("ponderacionesPorOrden", ponderacionesPorOrden);
    }
}
