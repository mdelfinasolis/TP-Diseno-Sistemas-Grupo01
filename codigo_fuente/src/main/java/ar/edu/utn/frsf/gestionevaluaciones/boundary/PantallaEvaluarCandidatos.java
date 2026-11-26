package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.control.GestorCandidatos;
import ar.edu.utn.frsf.gestionevaluaciones.control.GestorEvaluaciones;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.EvaluarCandidatosEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroCandidatoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFuncionEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.excel.ExportacionCandidatosExcelDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.excel.ExportarClavesExcelEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoClaveDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoEvaluadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CompetenciaRequeridaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.FuncionBusquedaDTO;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CU025 - Evaluar a Candidatos. Pantalla 1: búsqueda y "Candidatos a evaluar";
 * pantalla 2: selección de la función o puesto; pantalla 3: candidatos con sus claves y exportación.
 * La exportación se descarga desde el navegador (reemplaza "solicitar carpeta destino").
 */
@Controller
@RequestMapping("/evaluar")
public class PantallaEvaluarCandidatos {

    static final String MENSAJE_SIN_CANDIDATOS = "Se debe seleccionar al menos un candidato.";
    static final String MENSAJE_SIN_FUNCION = "Se debe seleccionar al menos una función o puesto.";

    private static final String SESION_FILTROS = "cu025.filtros";
    private static final String SESION_ENCONTRADOS = "cu025.encontrados";
    private static final String SESION_PAGINA = "cu025.pagina";
    private static final String SESION_A_EVALUAR = "cu025.aEvaluar";
    private static final String SESION_FUNCIONES = "cu025.funciones";
    private static final String SESION_PUESTO = "cu025.puesto";
    private static final String SESION_COMPETENCIAS = "cu025.competencias";
    private static final String SESION_CLAVES = "cu025.claves";
    private static final String SESION_ID_EVALUACION = "cu025.idEvaluacion";
    private static final DateTimeFormatter FORMATO_NOMBRE_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    private final GestorEvaluaciones gestorEvaluaciones;
    private final GestorCandidatos gestorCandidatos;

    @Value("${app.paginacion.filas-por-pagina:10}")
    private int filasPorPagina;

    @Value("${app.cuestionario.preguntas-por-bloque}")
    private int preguntasPorBloque;

    public PantallaEvaluarCandidatos(GestorEvaluaciones gestorEvaluaciones, GestorCandidatos gestorCandidatos) {
        this.gestorEvaluaciones = gestorEvaluaciones;
        this.gestorCandidatos = gestorCandidatos;
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

    // =====================================================================
    // Pantalla 1: búsqueda de candidatos y candidatos a evaluar
    // =====================================================================

    /** El Consultor selecciona la opción "Evaluar Candidatos": se muestran los filtros. */
    @GetMapping
    public String seleccionarOpcionEvaluarCandidatos(Model model, HttpSession session) {
        limpiarSesion(session);
        return mostrarPantallaCandidatos(null, model, session);
    }

    /** Botón Buscar (paso 3). */
    @PostMapping("/buscar")
    public String buscarCandidatos(@ModelAttribute("filtros") FiltroCandidatoEntradaDTO filtros,
                                   BindingResult resultado, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (resultado.hasFieldErrors("nroCandidato")) {
            redirectAttributes.addFlashAttribute("mensajeError", "El número de candidato debe ser numérico.");
            return "redirect:/evaluar/candidatos";
        }
        session.setAttribute(SESION_FILTROS, filtros);
        session.setAttribute(SESION_ENCONTRADOS, new ArrayList<>(gestorCandidatos.buscarCandidatos(filtros)));
        session.setAttribute(SESION_PAGINA, 1);
        return "redirect:/evaluar/candidatos";
    }

    @GetMapping("/candidatos")
    @SuppressWarnings("unchecked")
    public String mostrarPantallaCandidatos(@RequestParam(required = false) Integer pagina, Model model,
                                            HttpSession session) {
        FiltroCandidatoEntradaDTO filtros = (FiltroCandidatoEntradaDTO) session.getAttribute(SESION_FILTROS);
        List<CandidatoEvaluadoDTO> encontrados = (List<CandidatoEvaluadoDTO>) session.getAttribute(SESION_ENCONTRADOS);
        model.addAttribute("filtros", filtros != null ? filtros : new FiltroCandidatoEntradaDTO());
        model.addAttribute("aEvaluar", aEvaluar(session).values());
        model.addAttribute("mostrarGrilla", encontrados != null);

        if (encontrados != null) {
            if (pagina == null) {
                Integer guardada = (Integer) session.getAttribute(SESION_PAGINA);
                pagina = guardada != null ? guardada : 1;
            }
            int totalPaginas = Math.max(1, (int) Math.ceil(encontrados.size() / (double) filasPorPagina));
            pagina = Math.min(Math.max(1, pagina), totalPaginas);
            session.setAttribute(SESION_PAGINA, pagina);
            int desde = (pagina - 1) * filasPorPagina;
            int hasta = Math.min(desde + filasPorPagina, encontrados.size());
            model.addAttribute("encontrados", encontrados.subList(desde, hasta));
            model.addAttribute("totalResultados", encontrados.size());
            model.addAttribute("pagina", pagina);
            model.addAttribute("totalPaginas", totalPaginas);
        }
        return "evaluarCandidatos";
    }

    /** El Consultor selecciona candidatos en la grilla; devuelve false si no seleccionó ninguno (4.A). */
    public boolean seleccionarCandidatos(List<Integer> nrosCandidatos) {
        return nrosCandidatos != null && !nrosCandidatos.isEmpty();
    }

    /** Botón Agregar (pasos 4 y 5). */
    @PostMapping("/agregar")
    @SuppressWarnings("unchecked")
    public String agregar(@RequestParam(name = "nrosCandidatos", required = false) List<Integer> nrosCandidatos,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        if (!seleccionarCandidatos(nrosCandidatos)) {
            redirectAttributes.addFlashAttribute("mensajeError", MENSAJE_SIN_CANDIDATOS);
            return "redirect:/evaluar/candidatos";
        }
        List<CandidatoEvaluadoDTO> encontrados = (List<CandidatoEvaluadoDTO>) session.getAttribute(SESION_ENCONTRADOS);
        Map<Integer, CandidatoEvaluadoDTO> aEvaluar = aEvaluar(session);
        if (encontrados != null) {
            encontrados.stream()
                    .filter(c -> nrosCandidatos.contains(c.getNroCandidato()))
                    .forEach(c -> aEvaluar.putIfAbsent(c.getNroCandidato(), c));
        }
        session.setAttribute(SESION_A_EVALUAR, aEvaluar);
        return "redirect:/evaluar/candidatos";
    }

    /** Botón Quitar (5.A). */
    @PostMapping("/quitar")
    public String quitar(@RequestParam(name = "nrosCandidatos", required = false) List<Integer> nrosCandidatos,
                         HttpSession session, RedirectAttributes redirectAttributes) {
        if (!seleccionarCandidatos(nrosCandidatos)) {
            redirectAttributes.addFlashAttribute("mensajeError", MENSAJE_SIN_CANDIDATOS);
            return "redirect:/evaluar/candidatos";
        }
        Map<Integer, CandidatoEvaluadoDTO> aEvaluar = aEvaluar(session);
        nrosCandidatos.forEach(aEvaluar::remove);
        session.setAttribute(SESION_A_EVALUAR, aEvaluar);
        return "redirect:/evaluar/candidatos";
    }

    /** Botón Siguiente de la pantalla 1 (paso 6): verifica cuestionarios activos o en proceso (6.A). */
    @PostMapping("/siguiente")
    public String siguiente(HttpSession session, RedirectAttributes redirectAttributes) {
        Map<Integer, CandidatoEvaluadoDTO> aEvaluar = aEvaluar(session);
        if (aEvaluar.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", MENSAJE_SIN_CANDIDATOS);
            return "redirect:/evaluar/candidatos";
        }
        List<CandidatoEvaluadoDTO> noEvaluables =
                gestorEvaluaciones.verificarCuestionariosActivos(new ArrayList<>(aEvaluar.keySet()));
        if (!noEvaluables.isEmpty()) {
            // 6.A.2: se retorna a "Candidatos a evaluar" para poder quitarlos
            redirectAttributes.addFlashAttribute("noEvaluables", noEvaluables);
            return "redirect:/evaluar/candidatos";
        }
        List<FuncionBusquedaDTO> funciones = gestorEvaluaciones.buscarFunciones(new FiltroFuncionEntradaDTO());
        session.setAttribute(SESION_FUNCIONES, new ArrayList<>(funciones));
        session.removeAttribute(SESION_PUESTO);
        session.removeAttribute(SESION_COMPETENCIAS);
        return "redirect:/evaluar/puesto";
    }

    // =====================================================================
    // Pantalla 2: selección de la función o puesto
    // =====================================================================

    @GetMapping("/puesto")
    public String mostrarPantallaPuesto(Model model, HttpSession session) {
        if (session.getAttribute(SESION_FUNCIONES) == null) {
            return "redirect:/evaluar";
        }
        model.addAttribute("funciones", session.getAttribute(SESION_FUNCIONES));
        model.addAttribute("puesto", session.getAttribute(SESION_PUESTO));
        model.addAttribute("competencias", session.getAttribute(SESION_COMPETENCIAS));
        return "evaluarPuesto";
    }

    /** El Consultor selecciona la función o puesto (paso 8) y presiona Buscar: competencias requeridas (paso 9). */
    @PostMapping("/puesto")
    @SuppressWarnings("unchecked")
    public String seleccionarPuesto(@RequestParam(required = false) String codigoPuesto, HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        List<FuncionBusquedaDTO> funciones = (List<FuncionBusquedaDTO>) session.getAttribute(SESION_FUNCIONES);
        if (funciones == null) {
            return "redirect:/evaluar";
        }
        FuncionBusquedaDTO puesto = funciones.stream()
                .filter(f -> f.getCodigo().equals(codigoPuesto))
                .findFirst()
                .orElse(null);
        if (puesto == null) {
            redirectAttributes.addFlashAttribute("mensajeError", MENSAJE_SIN_FUNCION);
            return "redirect:/evaluar/puesto";
        }
        List<CompetenciaRequeridaDTO> competencias = gestorEvaluaciones.listarCompetenciasRequeridas(puesto.getCodigo());
        session.setAttribute(SESION_PUESTO, puesto);
        session.setAttribute(SESION_COMPETENCIAS, new ArrayList<>(competencias));

        List<String> noEvaluables = competencias.stream()
                .filter(c -> !c.isEvaluable())
                .map(CompetenciaRequeridaDTO::getCompetencia)
                .toList();
        if (!noEvaluables.isEmpty()) {
            // 9.A: se muestran las competencias que no pueden evaluarse; Aceptar finaliza el CU
            redirectAttributes.addFlashAttribute("competenciasNoEvaluables", noEvaluables);
        }
        return "redirect:/evaluar/puesto";
    }

    /** Botón Siguiente de la pantalla 2 (paso 10): genera las claves de ingreso (paso 11). */
    @PostMapping("/puesto/siguiente")
    @SuppressWarnings("unchecked")
    public String siguiente(@RequestParam(required = false) String codigoPuesto, HttpSession session,
                            RedirectAttributes redirectAttributes) {
        FuncionBusquedaDTO puesto = (FuncionBusquedaDTO) session.getAttribute(SESION_PUESTO);
        if (puesto == null || codigoPuesto == null || !codigoPuesto.equals(puesto.getCodigo())) {
            // 10.A: no se seleccionó (o no se buscó) la función o puesto
            redirectAttributes.addFlashAttribute("mensajeError", MENSAJE_SIN_FUNCION);
            return "redirect:/evaluar/puesto";
        }
        List<CompetenciaRequeridaDTO> competencias =
                (List<CompetenciaRequeridaDTO>) session.getAttribute(SESION_COMPETENCIAS);
        if (competencias == null || competencias.stream().anyMatch(c -> !c.isEvaluable())) {
            return "redirect:/evaluar/puesto";
        }
        List<CandidatoClaveDTO> claves =
                gestorEvaluaciones.generarClaves(new ArrayList<>(aEvaluar(session).keySet()));
        session.setAttribute(SESION_CLAVES, new ArrayList<>(claves));
        return "redirect:/evaluar/claves";
    }

    // =====================================================================
    // Pantalla 3: candidatos con claves, finalizar y exportar
    // =====================================================================

    @GetMapping("/claves")
    public String mostrarPantallaClaves(Model model, HttpSession session) {
        if (session.getAttribute(SESION_CLAVES) == null) {
            return "redirect:/evaluar";
        }
        model.addAttribute("claves", session.getAttribute(SESION_CLAVES));
        model.addAttribute("puesto", session.getAttribute(SESION_PUESTO));
        // Paso 13: evaluación guardada -> se pregunta si desea exportar
        model.addAttribute("evaluacionGuardada", session.getAttribute(SESION_ID_EVALUACION) != null);
        return "evaluarClaves";
    }

    /** Botón Finalizar (paso 12): crea la evaluación y los cuestionarios ACTIVO (paso 13). */
    @PostMapping("/finalizar")
    @SuppressWarnings("unchecked")
    public String finalizar(HttpSession session, RedirectAttributes redirectAttributes) {
        List<CandidatoClaveDTO> claves = (List<CandidatoClaveDTO>) session.getAttribute(SESION_CLAVES);
        FuncionBusquedaDTO puesto = (FuncionBusquedaDTO) session.getAttribute(SESION_PUESTO);
        if (claves == null || puesto == null) {
            return "redirect:/evaluar";
        }
        if (session.getAttribute(SESION_ID_EVALUACION) != null) {
            return "redirect:/evaluar/claves"; // ya se guardó (doble click o recarga)
        }
        EvaluarCandidatosEntradaDTO datos = new EvaluarCandidatosEntradaDTO(puesto.getCodigo(), claves, preguntasPorBloque);
        try {
            int idEvaluacion = gestorEvaluaciones.evaluarCandidatos(datos);
            session.setAttribute(SESION_ID_EVALUACION, idEvaluacion);
        } catch (IllegalStateException e) {
            // Otro consultor evaluó a alguno de los candidatos mientras tanto
            List<CandidatoEvaluadoDTO> noEvaluables =
                    gestorEvaluaciones.verificarCuestionariosActivos(new ArrayList<>(aEvaluar(session).keySet()));
            redirectAttributes.addFlashAttribute("noEvaluables", noEvaluables);
            return "redirect:/evaluar/candidatos";
        }
        return "redirect:/evaluar/claves";
    }

    /** Paso 15: diálogo de exportación con el nombre de archivo aaaa-mm-dd-hh-mm-NombreFunción.xls. */
    @GetMapping("/exportar")
    public String mostrarDialogoExportacion(Model model, HttpSession session) {
        FuncionBusquedaDTO puesto = (FuncionBusquedaDTO) session.getAttribute(SESION_PUESTO);
        if (session.getAttribute(SESION_ID_EVALUACION) == null || puesto == null) {
            return "redirect:/evaluar";
        }
        model.addAttribute("nombreArchivo",
                LocalDateTime.now().format(FORMATO_NOMBRE_ARCHIVO) + "-" + puesto.getNombre() + ".xls");
        return "evaluarExportar";
    }

    /** Botón Guardar (paso 16): genera el .xls con los candidatos y claves persistidos y lo descarga. */
    @PostMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam(required = false) String nombreArchivo, HttpSession session)
            throws IOException {
        Integer idEvaluacion = (Integer) session.getAttribute(SESION_ID_EVALUACION);
        if (idEvaluacion == null) {
            return ResponseEntity.badRequest().build();
        }
        ExportacionCandidatosExcelDTO exportacion = gestorEvaluaciones.exportarCandidatosConClaves(
                new ExportarClavesExcelEntradaDTO(idEvaluacion, null, nombreArchivo));
        byte[] contenido = generarArchivoExcel(exportacion);
        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(exportacion.getArchivo().getNombreArchivo(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(contenido);
    }

    /**
     * Botón Cancelar / Salir. En el diálogo de exportación (paso=exportacion) vuelve a la pregunta
     * de exportación (16.A); en el resto finaliza el CU y vuelve a la pantalla principal.
     */
    @GetMapping("/cancelar")
    public String cancelar(@RequestParam(required = false) String paso, HttpSession session) {
        if ("exportacion".equalsIgnoreCase(paso) && session.getAttribute(SESION_ID_EVALUACION) != null) {
            return "redirect:/evaluar/claves";
        }
        limpiarSesion(session);
        return "redirect:/menu";
    }

    private byte[] generarArchivoExcel(ExportacionCandidatosExcelDTO exportacion) throws IOException {
        try (HSSFWorkbook libro = new HSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            libro.createInformationProperties();
            libro.getSummaryInformation().setTitle("Candidatos y claves de ingreso");
            Sheet hoja = libro.createSheet("Candidatos");
            CellStyle estiloEncabezado = libro.createCellStyle();
            Font negrita = libro.createFont();
            negrita.setBold(true);
            estiloEncabezado.setFont(negrita);

            String[] columnas = {"Apellido", "Nombre", "Tipo de Documento", "Número de Documento", "Clave de Ingreso"};
            Row encabezado = hoja.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                encabezado.createCell(i).setCellValue(columnas[i]);
                encabezado.getCell(i).setCellStyle(estiloEncabezado);
            }
            int fila = 1;
            for (CandidatoClaveDTO c : exportacion.getCandidatos()) {
                Row row = hoja.createRow(fila++);
                row.createCell(0).setCellValue(c.getApellido());
                row.createCell(1).setCellValue(c.getNombre());
                row.createCell(2).setCellValue(c.getTipoDocumento().name());
                row.createCell(3).setCellValue(c.getNroDocumento());
                row.createCell(4).setCellValue(c.getClaveIngreso());
            }
            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
            }
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, CandidatoEvaluadoDTO> aEvaluar(HttpSession session) {
        Object guardado = session.getAttribute(SESION_A_EVALUAR);
        return guardado != null ? (Map<Integer, CandidatoEvaluadoDTO>) guardado : new LinkedHashMap<>();
    }

    private void limpiarSesion(HttpSession session) {
        for (String atributo : List.of(SESION_FILTROS, SESION_ENCONTRADOS, SESION_PAGINA, SESION_A_EVALUAR,
                SESION_FUNCIONES, SESION_PUESTO, SESION_COMPETENCIAS, SESION_CLAVES, SESION_ID_EVALUACION)) {
            session.removeAttribute(atributo);
        }
    }
}
