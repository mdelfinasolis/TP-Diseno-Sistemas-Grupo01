package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.control.GestorEmpresas;
import ar.edu.utn.frsf.gestionevaluaciones.control.GestorPuestos;
import ar.edu.utn.frsf.gestionevaluaciones.control.GestorReportes;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFuncionEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.OrdenMeritoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.EmpresaListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.EvaluacionListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.OrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PuestoListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CU028 - Emitir orden de mérito. Solo recibe y envía DTOs.
 * La impresión (imprimir()) la resuelve el navegador del Consultor con window.print().
 */
@Controller
@RequestMapping("/orden-merito")
public class PantallaOrdenMerito {

    static final String MENSAJE_SIN_EVALUACIONES = "El puesto seleccionado no posee evaluaciones.";

    private static final String SESION_PUESTOS = "cu028.listaPuestos";
    private static final String SESION_EMPRESAS = "cu028.listaEmpresas";
    private static final String SESION_FILTROS = "cu028.filtros";
    private static final String SESION_RESULTADOS = "cu028.resultados";
    private static final String SESION_PAGINA = "cu028.pagina";
    private static final String SESION_PUESTO = "cu028.puestoSeleccionado";
    private static final String SESION_EVALUACIONES = "cu028.evaluaciones";
    private static final String SESION_ORDEN = "cu028.ordenMerito";

    private final GestorReportes gestorReportes;
    private final GestorPuestos gestorPuestos;
    private final GestorEmpresas gestorEmpresas;

    @Value("${app.paginacion.filas-por-pagina:10}")
    private int filasPorPagina;

    public PantallaOrdenMerito(GestorReportes gestorReportes, GestorPuestos gestorPuestos,
                               GestorEmpresas gestorEmpresas) {
        this.gestorReportes = gestorReportes;
        this.gestorPuestos = gestorPuestos;
        this.gestorEmpresas = gestorEmpresas;
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

    /** Paso 1: el Consultor elige la opción; se cargan las listas desplegables de puestos y empresas. */
    @GetMapping
    public String seleccionarOpcionOrdenMerito(Model model, HttpSession session) {
        limpiarSesion(session);
        List<PuestoListadoDTO> puestos = gestorPuestos.listarPuestos();
        List<EmpresaListadoDTO> empresas = gestorEmpresas.listarEmpresas();
        session.setAttribute(SESION_PUESTOS, puestos);
        session.setAttribute(SESION_EMPRESAS, empresas);

        cargarFiltros(model, session, new FiltroFuncionEntradaDTO());
        model.addAttribute("mostrarGrilla", false);
        return "ordenMeritoPuestos";
    }

    /** alt [actor presiona Cancelar]: fin del CU. También lo usa el botón Salir. */
    @GetMapping("/cancelar")
    public String cancelar(HttpSession session) {
        limpiarSesion(session);
        return "redirect:/menu";
    }

    /** El Consultor ingresa los filtros: Código, Nombre del puesto y Empresa. */
    public void ingresarFiltros(FiltroFuncionEntradaDTO filtros, HttpSession session) {
        session.setAttribute(SESION_FILTROS, filtros);
    }

    /** Botón Buscar: busca los puestos con su cantidad de candidatos y evaluaciones completas. */
    @PostMapping("/buscar")
    public String buscar(@ModelAttribute("filtros") FiltroFuncionEntradaDTO filtros, HttpSession session) {
        if (session.getAttribute(SESION_PUESTOS) == null) {
            return "redirect:/orden-merito";
        }
        ingresarFiltros(filtros, session);
        List<PuestoOrdenMeritoDTO> resultados = gestorReportes.buscarPuestosOrdenMerito(filtros);
        session.setAttribute(SESION_RESULTADOS, new ArrayList<>(resultados));
        session.setAttribute(SESION_PAGINA, 1);
        return "redirect:/orden-merito/puestos";
    }

    /** Grilla de puestos (con paginación). También es a donde se vuelve en el flujo alternativo 6.A. */
    @GetMapping("/puestos")
    @SuppressWarnings("unchecked")
    public String mostrarGrillaPuestos(@RequestParam(required = false) Integer pagina, Model model,
                                       HttpSession session) {
        List<PuestoOrdenMeritoDTO> resultados = (List<PuestoOrdenMeritoDTO>) session.getAttribute(SESION_RESULTADOS);
        if (resultados == null) {
            return "redirect:/orden-merito";
        }
        if (pagina == null) {
            Integer paginaGuardada = (Integer) session.getAttribute(SESION_PAGINA);
            pagina = paginaGuardada != null ? paginaGuardada : 1;
        }
        int totalPaginas = Math.max(1, (int) Math.ceil(resultados.size() / (double) filasPorPagina));
        pagina = Math.min(Math.max(1, pagina), totalPaginas);
        session.setAttribute(SESION_PAGINA, pagina);
        int desde = (pagina - 1) * filasPorPagina;
        int hasta = Math.min(desde + filasPorPagina, resultados.size());

        FiltroFuncionEntradaDTO filtros = (FiltroFuncionEntradaDTO) session.getAttribute(SESION_FILTROS);
        cargarFiltros(model, session, filtros != null ? filtros : new FiltroFuncionEntradaDTO());
        model.addAttribute("mostrarGrilla", true);
        model.addAttribute("puestos", resultados.subList(desde, hasta));
        model.addAttribute("totalResultados", resultados.size());
        model.addAttribute("pagina", pagina);
        model.addAttribute("totalPaginas", totalPaginas);
        return "ordenMeritoPuestos";
    }

    /** Botón "Emitir el orden de mérito" con el puesto seleccionado. */
    @PostMapping("/emitir")
    @SuppressWarnings("unchecked")
    public String emitirOrdenMerito(@RequestParam("codigoPuesto") String codigoPuesto, HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        List<PuestoOrdenMeritoDTO> resultados = (List<PuestoOrdenMeritoDTO>) session.getAttribute(SESION_RESULTADOS);
        if (resultados == null) {
            return "redirect:/orden-merito";
        }
        PuestoOrdenMeritoDTO puesto = resultados.stream()
                .filter(p -> p.getCodigo().equals(codigoPuesto))
                .findFirst()
                .orElse(null);
        if (puesto == null) {
            return "redirect:/orden-merito/puestos";
        }

        List<EvaluacionListadoDTO> evaluaciones = gestorReportes.listarEvaluacionesPuesto(codigoPuesto);

        if (evaluaciones.isEmpty()) {
            // alt [la lista de evaluaciones está vacía]: mensaje y vuelve a la grilla de puestos
            mostrarMensaje(MENSAJE_SIN_EVALUACIONES, redirectAttributes);
            return "redirect:/orden-merito/puestos";
        }

        session.setAttribute(SESION_PUESTO, puesto);
        session.setAttribute(SESION_EVALUACIONES, new ArrayList<>(evaluaciones));
        return "redirect:/orden-merito/evaluaciones";
    }

    /** Muestra un mensaje en una ventana sobre la pantalla actual. */
    public void mostrarMensaje(String mensaje, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensaje", mensaje);
    }

    /** Pantalla 2: evaluaciones del puesto + opción TODAS. */
    @GetMapping("/evaluaciones")
    public String mostrarEvaluaciones(Model model, HttpSession session) {
        PuestoOrdenMeritoDTO puesto = (PuestoOrdenMeritoDTO) session.getAttribute(SESION_PUESTO);
        Object evaluaciones = session.getAttribute(SESION_EVALUACIONES);
        if (puesto == null || evaluaciones == null) {
            return "redirect:/orden-merito";
        }
        model.addAttribute("puesto", puesto);
        model.addAttribute("evaluaciones", evaluaciones);
        return "ordenMeritoEvaluaciones";
    }

    /** El Consultor selecciona las evaluaciones (o TODAS) y presiona Emitir. */
    @PostMapping("/evaluaciones")
    public String seleccionarEvaluaciones(@RequestParam(name = "idsEvaluaciones", required = false) List<Integer> idsEvaluaciones,
                                          @RequestParam(name = "todasLasEvaluaciones", defaultValue = "false") boolean todasLasEvaluaciones,
                                          HttpSession session) {
        PuestoOrdenMeritoDTO puesto = (PuestoOrdenMeritoDTO) session.getAttribute(SESION_PUESTO);
        if (puesto == null) {
            return "redirect:/orden-merito";
        }
        if (!todasLasEvaluaciones && (idsEvaluaciones == null || idsEvaluaciones.isEmpty())) {
            return "redirect:/orden-merito/evaluaciones";
        }

        OrdenMeritoEntradaDTO datos = new OrdenMeritoEntradaDTO(puesto.getCodigo(), idsEvaluaciones, todasLasEvaluaciones);
        OrdenMeritoDTO ordenMerito = gestorReportes.emitirOrdenMerito(datos);
        session.setAttribute(SESION_ORDEN, ordenMerito);
        return "redirect:/orden-merito/listado";
    }

    /**
     * Pantalla 3: listado de candidatos evaluados en tres secciones:
     * orden de mérito, no alcanzan puntaje mínimo y cuestionarios incompletos/sin contestar (agrupados por estado).
     */
    @GetMapping("/listado")
    public String mostrarOrdenMerito(Model model, HttpSession session) {
        OrdenMeritoDTO ordenMerito = (OrdenMeritoDTO) session.getAttribute(SESION_ORDEN);
        if (ordenMerito == null) {
            return "redirect:/orden-merito";
        }

        List<CandidatoOrdenMeritoDTO> enOrden = new ArrayList<>();
        List<CandidatoOrdenMeritoDTO> noAlcanzan = new ArrayList<>();
        Map<ESTADOCUESTIONARIO, List<CandidatoOrdenMeritoDTO>> otrosPorEstado = new LinkedHashMap<>();
        for (CandidatoOrdenMeritoDTO c : ordenMerito.getCandidatos()) {
            if (c.getEstadoCuestionario() == ESTADOCUESTIONARIO.COMPLETO) {
                (c.isCumpleMinimos() ? enOrden : noAlcanzan).add(c);
            } else {
                otrosPorEstado.computeIfAbsent(c.getEstadoCuestionario(), e -> new ArrayList<>()).add(c);
            }
        }

        model.addAttribute("ordenMerito", ordenMerito);
        model.addAttribute("enOrden", enOrden);
        model.addAttribute("noAlcanzan", noAlcanzan);
        model.addAttribute("otrosPorEstado", otrosPorEstado);
        return "ordenMerito";
    }

    private void cargarFiltros(Model model, HttpSession session, FiltroFuncionEntradaDTO filtros) {
        model.addAttribute("filtros", filtros);
        model.addAttribute("listaPuestos", session.getAttribute(SESION_PUESTOS));
        model.addAttribute("listaEmpresas", session.getAttribute(SESION_EMPRESAS));
    }

    private void limpiarSesion(HttpSession session) {
        for (String atributo : List.of(SESION_PUESTOS, SESION_EMPRESAS, SESION_FILTROS, SESION_RESULTADOS,
                SESION_PAGINA, SESION_PUESTO, SESION_EVALUACIONES, SESION_ORDEN)) {
            session.removeAttribute(atributo);
        }
    }
}
