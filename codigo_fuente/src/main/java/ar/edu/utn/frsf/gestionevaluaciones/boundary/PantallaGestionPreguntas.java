package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.control.GestorCompetencias;
import ar.edu.utn.frsf.gestionevaluaciones.control.GestorFactores;
import ar.edu.utn.frsf.gestionevaluaciones.control.GestorPreguntas;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFactorEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroPreguntaEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.FactorListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PreguntaListadoDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;
import java.util.Locale;

/**
 * CU020 - Gestionar Preguntas. Solo recibe y envía DTOs.
 * Los botones Nuevo / Modificar / Eliminar invocan CU021 / CU022 / CU023 (por ahora "en construcción");
 * al volver de CU022 o CU023 la grilla se actualiza con buscarPreguntas(filtros).
 */
@Controller
@RequestMapping("/preguntas")
public class PantallaGestionPreguntas {

    private static final String SESION_FILTROS = "cu020.filtros";
    private static final String SESION_PAGINA = "cu020.pagina";
    private static final String SESION_PREGUNTA = "cu020.preguntaSeleccionada";

    private final GestorPreguntas gestorPreguntas;
    private final GestorCompetencias gestorCompetencias;
    private final GestorFactores gestorFactores;

    @Value("${app.consultor.nombre}")
    private String nombreConsultor;

    @Value("${app.paginacion.filas-por-pagina:10}")
    private int filasPorPagina;

    public PantallaGestionPreguntas(GestorPreguntas gestorPreguntas,
                                    GestorCompetencias gestorCompetencias,
                                    GestorFactores gestorFactores) {
        this.gestorPreguntas = gestorPreguntas;
        this.gestorCompetencias = gestorCompetencias;
        this.gestorFactores = gestorFactores;
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

    /** Paso 1: el Consultor elige la opción Preguntas; se cargan las competencias y se muestran los criterios. */
    @GetMapping
    public String seleccionarOpcionPreguntas(Model model, HttpSession session) {
        session.removeAttribute(SESION_FILTROS);
        session.removeAttribute(SESION_PAGINA);
        session.removeAttribute(SESION_PREGUNTA);
        cargarCriterios(model, new FiltroPreguntaEntradaDTO());
        model.addAttribute("mostrarGrilla", false);
        return "gestionPreguntas";
    }

    /** opt [actor selecciona una competencia]: carga los factores de esa competencia. */
    @GetMapping("/factores")
    @ResponseBody
    public List<FactorListadoDTO> seleccionarCompetencia(@RequestParam(required = false) String codigoCompetencia) {
        if (codigoCompetencia == null || codigoCompetencia.isBlank()) {
            return List.of();
        }
        return gestorFactores.buscarFactores(new FiltroFactorEntradaDTO(codigoCompetencia, ""));
    }

    /** El Consultor ingresa los filtros (se guardan para poder actualizar la grilla al volver de CU022/CU023). */
    public void ingresarFiltros(FiltroPreguntaEntradaDTO filtros, HttpSession session) {
        session.setAttribute(SESION_FILTROS, filtros);
        session.setAttribute(SESION_PAGINA, 1);
    }

    /** Botón Buscar. Sin filtros se devuelven todas las preguntas. */
    @PostMapping("/buscar")
    public String buscar(@ModelAttribute("filtros") FiltroPreguntaEntradaDTO filtros, HttpSession session) {
        ingresarFiltros(filtros, session);
        return "redirect:/preguntas/grilla";
    }

    /** Muestra (o actualiza) la grilla ejecutando buscarPreguntas(filtros) con los filtros ingresados. */
    @GetMapping("/grilla")
    public String mostrarGrilla(@RequestParam(required = false) Integer pagina, Model model, HttpSession session) {
        FiltroPreguntaEntradaDTO filtros = (FiltroPreguntaEntradaDTO) session.getAttribute(SESION_FILTROS);
        if (filtros == null) {
            return "redirect:/preguntas";
        }
        if (pagina == null) {
            Integer paginaGuardada = (Integer) session.getAttribute(SESION_PAGINA);
            pagina = paginaGuardada != null ? paginaGuardada : 1;
        }

        List<PreguntaListadoDTO> preguntas = gestorPreguntas.buscarPreguntas(filtros);

        int totalPaginas = Math.max(1, (int) Math.ceil(preguntas.size() / (double) filasPorPagina));
        pagina = Math.min(Math.max(1, pagina), totalPaginas);
        session.setAttribute(SESION_PAGINA, pagina);
        int desde = (pagina - 1) * filasPorPagina;
        int hasta = Math.min(desde + filasPorPagina, preguntas.size());

        cargarCriterios(model, filtros);
        model.addAttribute("mostrarGrilla", true);
        model.addAttribute("preguntas", preguntas.subList(desde, hasta));
        model.addAttribute("totalResultados", preguntas.size());
        model.addAttribute("pagina", pagina);
        model.addAttribute("totalPaginas", totalPaginas);
        return "gestionPreguntas";
    }

    /** Vuelta de CU021: se muestran nuevamente los criterios de búsqueda. */
    @GetMapping("/criterios")
    public String volverACriterios(Model model, HttpSession session) {
        FiltroPreguntaEntradaDTO filtros = (FiltroPreguntaEntradaDTO) session.getAttribute(SESION_FILTROS);
        cargarCriterios(model, filtros != null ? filtros : new FiltroPreguntaEntradaDTO());
        model.addAttribute("mostrarGrilla", false);
        return "gestionPreguntas";
    }

    /**
     * alt [actor presiona Nuevo]: ref CU021 - Dar de alta Pregunta (PantallaAltaPregunta.abrir()).
     * Al terminar CU021 se vuelve a los criterios de búsqueda (/preguntas/criterios).
     */
    @GetMapping("/nuevo")
    public String nuevo(HttpSession session) {
        session.removeAttribute("cu021.datos");
        session.removeAttribute("cu021.codigoCompetencia");
        return "redirect:/preguntas/alta";
    }

    /** El Consultor selecciona una pregunta de la grilla. */
    public void seleccionar(String codigoPregunta, HttpSession session) {
        session.setAttribute(SESION_PREGUNTA, codigoPregunta);
    }

    /** alt [actor presiona Modificar]: ref CU022 - Modificar Pregunta. Al volver se actualiza la grilla. */
    @GetMapping("/modificar")
    public String modificar(@RequestParam("codigo") String codigoPregunta, Model model, HttpSession session) {
        seleccionar(codigoPregunta, session);
        model.addAttribute("tituloPantalla", "Modificar Pregunta");
        model.addAttribute("casoDeUso", "CU022 - Modificar Pregunta");
        model.addAttribute("preguntaSeleccionada", codigoPregunta);
        model.addAttribute("urlVolver", "/preguntas/grilla");
        return "enConstruccion";
    }

    /** alt [actor presiona Eliminar]: ref CU023 - Dar de baja Pregunta. Al volver se actualiza la grilla. */
    @GetMapping("/eliminar")
    public String eliminar(@RequestParam("codigo") String codigoPregunta, Model model, HttpSession session) {
        seleccionar(codigoPregunta, session);
        model.addAttribute("tituloPantalla", "Dar de baja Pregunta");
        model.addAttribute("casoDeUso", "CU023 - Dar de baja Pregunta");
        model.addAttribute("preguntaSeleccionada", codigoPregunta);
        model.addAttribute("urlVolver", "/preguntas/grilla");
        return "enConstruccion";
    }

    /** En cualquier momento el Consultor puede presionar Salir y finalizar el CU. */
    @GetMapping("/salir")
    public String salir(HttpSession session) {
        session.removeAttribute(SESION_FILTROS);
        session.removeAttribute(SESION_PAGINA);
        session.removeAttribute(SESION_PREGUNTA);
        return "redirect:/";
    }

    /** Carga las listas desplegables de los criterios de búsqueda. */
    private void cargarCriterios(Model model, FiltroPreguntaEntradaDTO filtros) {
        model.addAttribute("filtros", filtros);
        model.addAttribute("competencias", gestorCompetencias.listarCompetencias());
        model.addAttribute("factores", seleccionarCompetencia(filtros.getCodigoCompetencia()));
    }
}
