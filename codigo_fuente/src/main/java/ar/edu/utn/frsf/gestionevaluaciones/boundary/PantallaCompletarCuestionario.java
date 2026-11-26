package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.control.GestorCuestionarios;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FinalizarCuestionarioEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.GuardarBloqueCuestionarioEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.LoginCandidatoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.ObtenerBloqueCuestionarioEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.RespuestaPreguntaEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.BloqueCuestionarioDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CuestionarioSalidaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.OpcionCuestionarioDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PreguntaCuestionarioDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.ResultadoCuestionarioDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;
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

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CU026 - Completar Cuestionario (actor Candidato). El ingreso con tipo y número de documento y clave
 * sigue CU001 3.A. Las preguntas se muestran en bloques (wizard) sin posibilidad de volver atrás.
 */
@Controller
@RequestMapping("/cuestionario")
public class PantallaCompletarCuestionario {

    static final String MENSAJE_DATOS_INVALIDOS =
            "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato";
    static final String MENSAJE_TIEMPO_MAXIMO = "Tiempo máximo alcanzado.";
    static final String MENSAJE_ACCESOS = "Cantidad máxima de accesos alcanzada.";
    static final String MENSAJE_TIEMPO_ESPERA = "Tiempo de espera alcanzado.";

    private static final String SESION_CLAVE = "cu026.clave";
    private static final String SESION_CUESTIONARIO = "cu026.cuestionario";
    private static final String SESION_BLOQUE = "cu026.bloque";
    private static final String SESION_LIMITE = "cu026.limite";
    private static final String PREFIJO_RESPUESTA = "respuesta_";

    private final GestorCuestionarios gestorCuestionarios;

    @Value("${app.cuestionario.instrucciones.encabezado}")
    private String instruccionesEncabezado;
    @Value("${app.cuestionario.instrucciones.items}")
    private String instruccionesItems;
    @Value("${app.cuestionario.instrucciones.pie}")
    private String instruccionesPie;

    public PantallaCompletarCuestionario(GestorCuestionarios gestorCuestionarios) {
        this.gestorCuestionarios = gestorCuestionarios;
    }

    /** Los datos literales (incluida la clave) se ingresan siempre en mayúsculas. */
    @InitBinder
    public void convertirAMayusculas(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String texto) {
                setValue(texto == null || texto.isBlank() ? null : texto.trim().toUpperCase(Locale.ROOT));
            }
        });
    }

    /** Arriba a la derecha se muestra el nombre del candidato que ingresó. */
    @ModelAttribute("nombreUsuario")
    public String nombreUsuario(HttpSession session) {
        CuestionarioSalidaDTO cuestionario = (CuestionarioSalidaDTO) session.getAttribute(SESION_CUESTIONARIO);
        return cuestionario != null ? cuestionario.getNombreCandidato() : "";
    }

    // =====================================================================
    // Ingreso (CU001 3.A) y validaciones de acceso
    // =====================================================================

    /** Pantalla de ingreso del candidato: tipo y número de documento y clave. */
    @GetMapping
    public String mostrarIngreso(Model model, HttpSession session) {
        limpiarSesion(session);
        model.addAttribute("nombreUsuario", "");
        cargarIngreso(model, new LoginCandidatoEntradaDTO());
        return "cuestionarioIngreso";
    }

    @PostMapping
    public String completarCuestionario(@ModelAttribute("datos") LoginCandidatoEntradaDTO datos, Model model,
                                        HttpSession session) {
        limpiarSesion(session);
        CuestionarioSalidaDTO cuestionario = gestorCuestionarios.obtenerCuestionario(datos);

        if (!cuestionario.isValido()) {
            // CU001 3.A.2.2 a 3.A.2.4: mensaje de error y campos en blanco
            mostrarMensaje(MENSAJE_DATOS_INVALIDOS, model);
            model.addAttribute("nombreUsuario", "");
            cargarIngreso(model, new LoginCandidatoEntradaDTO());
            return "cuestionarioIngreso";
        }

        session.setAttribute(SESION_CUESTIONARIO, cuestionario);
        model.addAttribute("nombreUsuario", cuestionario.getNombreCandidato());

        if (cuestionario.getEstado() == ESTADOCUESTIONARIO.INCOMPLETO) {
            mostrarMensaje(cuestionario.getSegundosRestantes() == 0 ? MENSAJE_TIEMPO_MAXIMO : MENSAJE_ACCESOS, model);
            emitirAlertaAuditiva(model);
            return mostrarFin(model, session, ESTADOCUESTIONARIO.INCOMPLETO, false);
        }
        if (cuestionario.getEstado() == ESTADOCUESTIONARIO.SIN_CONTESTAR) {
            mostrarMensaje(MENSAJE_TIEMPO_ESPERA, model);
            emitirAlertaAuditiva(model);
            return mostrarFin(model, session, ESTADOCUESTIONARIO.SIN_CONTESTAR, false);
        }

        session.setAttribute(SESION_CLAVE, datos.getClaveIngreso());
        if (cuestionario.getEstado() == ESTADOCUESTIONARIO.EN_PROCESO) {
            // 1.C: se retoma en el primer bloque que aún no fue respondido
            session.setAttribute(SESION_BLOQUE, cuestionario.getBloqueActual());
            session.setAttribute(SESION_LIMITE, System.currentTimeMillis() + cuestionario.getSegundosRestantes() * 1000L);
            return "redirect:/cuestionario/bloque";
        }
        return "redirect:/cuestionario/instrucciones";
    }

    // =====================================================================
    // Instrucciones (cuestionario ACTIVO)
    // =====================================================================

    @GetMapping("/instrucciones")
    public String mostrarInstrucciones(Model model, HttpSession session) {
        CuestionarioSalidaDTO cuestionario = (CuestionarioSalidaDTO) session.getAttribute(SESION_CUESTIONARIO);
        if (cuestionario == null || session.getAttribute(SESION_CLAVE) == null) {
            return "redirect:/cuestionario";
        }
        model.addAttribute("cuestionario", cuestionario);
        model.addAttribute("instruccionesEncabezado", instruccionesEncabezado);
        model.addAttribute("instruccionesItems", Arrays.asList(instruccionesItems.split("\\|")));
        model.addAttribute("instruccionesPie", instruccionesPie);
        model.addAttribute("tiempoTotal", formatearTiempo(cuestionario.getTiempoMaximo() * 60L));
        return "cuestionarioInstrucciones";
    }

    /** Botón Aceptar de las instrucciones: se asignan las preguntas y comienza el cuestionario. */
    @PostMapping("/aceptar")
    public String aceptar(HttpSession session) {
        String clave = (String) session.getAttribute(SESION_CLAVE);
        CuestionarioSalidaDTO cuestionario = (CuestionarioSalidaDTO) session.getAttribute(SESION_CUESTIONARIO);
        if (clave == null || cuestionario == null) {
            return "redirect:/cuestionario";
        }
        gestorCuestionarios.iniciarCuestionario(clave);
        session.setAttribute(SESION_BLOQUE, 1);
        session.setAttribute(SESION_LIMITE, System.currentTimeMillis() + cuestionario.getTiempoMaximo() * 60_000L);
        return "redirect:/cuestionario/bloque";
    }

    /** Botón Cancelar (instrucciones) o Salir (wizard): los bloques ya guardados permanecen. */
    @GetMapping("/cancelar")
    public String cancelar(HttpSession session) {
        limpiarSesion(session);
        return "redirect:/cuestionario";
    }

    // =====================================================================
    // Wizard de bloques
    // =====================================================================

    @GetMapping("/bloque")
    public String mostrarBloque(Model model, HttpSession session) {
        String clave = (String) session.getAttribute(SESION_CLAVE);
        Integer nroBloque = (Integer) session.getAttribute(SESION_BLOQUE);
        if (clave == null || nroBloque == null) {
            return "redirect:/cuestionario";
        }
        ObtenerBloqueCuestionarioEntradaDTO datos = new ObtenerBloqueCuestionarioEntradaDTO(clave, nroBloque);
        BloqueCuestionarioDTO bloque = gestorCuestionarios.obtenerBloque(datos);
        if (nroBloque > bloque.getCantidadBloques()) {
            // Todos los bloques ya estaban guardados: se calculan los resultados y se finaliza
            return terminarCuestionario(model, session);
        }
        cargarBloque(model, session, bloque, Map.of());
        return "cuestionarioBloque";
    }

    /** El candidato elige una opción por pregunta (en el navegador, pantallas.js). */
    public void responderPreguntas() {
        // La selección de respuestas ocurre en el navegador; se reciben al presionar Siguiente / Finalizar.
    }

    /** Botón Siguiente: valida el bloque, verifica la vigencia y lo guarda. */
    @PostMapping("/siguiente")
    public String siguiente(@RequestParam Map<String, String> parametros, Model model, HttpSession session) {
        return procesarBloque(parametros, model, session);
    }

    /** Botón Finalizar (último bloque): guarda el bloque, calcula los resultados y completa el cuestionario. */
    @PostMapping("/finalizar")
    public String finalizar(@RequestParam Map<String, String> parametros, Model model, HttpSession session) {
        return procesarBloque(parametros, model, session);
    }

    /** Valida que se hayan respondido todas las preguntas del bloque (también se valida en el navegador). */
    public boolean validarPreguntasRespondidas(List<PreguntaCuestionarioDTO> preguntas, Map<String, Integer> respuestas) {
        return preguntas.stream().allMatch(p -> respuestas.containsKey(p.getCodigoPregunta()));
    }

    /** 6.A: lista las preguntas sin responder ("Pregunta N" según su posición en el bloque). */
    public void mostrarPreguntasSinResponder(List<PreguntaCuestionarioDTO> preguntas, Map<String, Integer> respuestas,
                                             Model model) {
        List<String> faltantes = new ArrayList<>();
        for (int i = 0; i < preguntas.size(); i++) {
            if (!respuestas.containsKey(preguntas.get(i).getCodigoPregunta())) {
                faltantes.add("Pregunta " + (i + 1));
            }
        }
        model.addAttribute("preguntasSinResponder", faltantes);
    }

    public void mostrarMensaje(String mensaje, Model model) {
        model.addAttribute("mensaje", mensaje);
    }

    /** La alerta auditiva la emite el navegador (pantallas.js, emitirAlertaAuditiva()). */
    public void emitirAlertaAuditiva(Model model) {
        model.addAttribute("alertaAuditiva", true);
    }

    private String procesarBloque(Map<String, String> parametros, Model model, HttpSession session) {
        String clave = (String) session.getAttribute(SESION_CLAVE);
        Integer nroBloque = (Integer) session.getAttribute(SESION_BLOQUE);
        if (clave == null || nroBloque == null) {
            return "redirect:/cuestionario";
        }
        ObtenerBloqueCuestionarioEntradaDTO datosBloque = new ObtenerBloqueCuestionarioEntradaDTO(clave, nroBloque);
        BloqueCuestionarioDTO bloque = gestorCuestionarios.obtenerBloque(datosBloque);
        List<PreguntaCuestionarioDTO> preguntas = gestorCuestionarios.listarPreguntasBloque(datosBloque);
        Map<String, Integer> respuestas = leerRespuestas(parametros, preguntas);

        if (!validarPreguntasRespondidas(preguntas, respuestas)) {
            // 6.A: se mantiene el MISMO bloque y las respuestas ya ingresadas
            mostrarPreguntasSinResponder(preguntas, respuestas, model);
            emitirAlertaAuditiva(model);
            cargarBloque(model, session, bloque, respuestas);
            return "cuestionarioBloque";
        }

        CuestionarioSalidaDTO vigencia = gestorCuestionarios.verificarVigencia(clave);
        if (vigencia.getEstado() == ESTADOCUESTIONARIO.INCOMPLETO) {
            mostrarMensaje(MENSAJE_TIEMPO_MAXIMO, model);
            return mostrarFin(model, session, ESTADOCUESTIONARIO.INCOMPLETO, true);
        }

        List<RespuestaPreguntaEntradaDTO> lista = new ArrayList<>();
        respuestas.forEach((codigo, orden) -> lista.add(new RespuestaPreguntaEntradaDTO(codigo, orden)));
        gestorCuestionarios.guardarBloque(new GuardarBloqueCuestionarioEntradaDTO(clave, nroBloque, lista));
        session.setAttribute(SESION_BLOQUE, nroBloque + 1);
        session.setAttribute(SESION_LIMITE, System.currentTimeMillis() + vigencia.getSegundosRestantes() * 1000L);

        // Solo se completa el cuestionario cuando se guardó el último bloque, aunque la solicitud
        // llegue por "Finalizar" desde otro bloque (por ejemplo, con el botón Atrás del navegador).
        if (nroBloque >= bloque.getCantidadBloques()) {
            return terminarCuestionario(model, session);
        }
        return "redirect:/cuestionario/bloque";
    }

    /** Último bloque: verifica la vigencia, calcula los resultados y pasa el cuestionario a COMPLETO. */
    private String terminarCuestionario(Model model, HttpSession session) {
        String clave = (String) session.getAttribute(SESION_CLAVE);
        CuestionarioSalidaDTO vigencia = gestorCuestionarios.verificarVigencia(clave);
        if (vigencia.getEstado() == ESTADOCUESTIONARIO.INCOMPLETO) {
            mostrarMensaje(MENSAJE_TIEMPO_MAXIMO, model);
            return mostrarFin(model, session, ESTADOCUESTIONARIO.INCOMPLETO, true);
        }
        gestorCuestionarios.calcularResultados(clave);
        ResultadoCuestionarioDTO resultado =
                gestorCuestionarios.finalizarCuestionario(new FinalizarCuestionarioEntradaDTO(clave));
        mostrarMensaje(resultado.getMensaje(), model);
        model.addAttribute("exito", true);
        return mostrarFin(model, session, resultado.getEstadoFinal(), false);
    }

    private String mostrarFin(Model model, HttpSession session, ESTADOCUESTIONARIO estado, boolean emitirAlerta) {
        if (emitirAlerta) {
            emitirAlertaAuditiva(model);
        }
        model.addAttribute("estado", estado);
        // El CU termina: se cierra la sesión del cuestionario (el nombre ya quedó en el modelo)
        session.removeAttribute(SESION_CLAVE);
        session.removeAttribute(SESION_BLOQUE);
        session.removeAttribute(SESION_LIMITE);
        return "cuestionarioFin";
    }

    private void cargarBloque(Model model, HttpSession session, BloqueCuestionarioDTO bloque,
                              Map<String, Integer> respuestas) {
        String clave = (String) session.getAttribute(SESION_CLAVE);
        ObtenerBloqueCuestionarioEntradaDTO datos = new ObtenerBloqueCuestionarioEntradaDTO(clave, bloque.getNroBloque());
        List<PreguntaCuestionarioDTO> preguntas = gestorCuestionarios.listarPreguntasBloque(datos);
        Map<String, List<OpcionCuestionarioDTO>> opciones = new LinkedHashMap<>();
        for (PreguntaCuestionarioDTO pregunta : preguntas) {
            opciones.put(pregunta.getCodigoPregunta(), gestorCuestionarios.listarOpcionesPregunta(pregunta.getCodigoPregunta()));
        }
        mostrarBloque(model, bloque, preguntas, opciones, respuestas);
        Long limite = (Long) session.getAttribute(SESION_LIMITE);
        model.addAttribute("limite", limite != null ? limite : System.currentTimeMillis());
    }

    /** Muestra el bloque con sus preguntas y opciones (y las respuestas ya ingresadas, si las hay). */
    public void mostrarBloque(Model model, BloqueCuestionarioDTO bloque, List<PreguntaCuestionarioDTO> preguntas,
                              Map<String, List<OpcionCuestionarioDTO>> opciones, Map<String, Integer> respuestas) {
        model.addAttribute("bloque", bloque);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("opciones", opciones);
        model.addAttribute("respuestas", respuestas);
        model.addAttribute("ultimoBloque", bloque.getNroBloque() >= bloque.getCantidadBloques());
        model.addAttribute("progreso", bloque.getCantidadBloques() == 0 ? 100
                : (int) Math.round(100.0 * bloque.getNroBloque() / bloque.getCantidadBloques()));
    }

    private static Map<String, Integer> leerRespuestas(Map<String, String> parametros,
                                                       List<PreguntaCuestionarioDTO> preguntas) {
        Map<String, Integer> respuestas = new LinkedHashMap<>();
        for (PreguntaCuestionarioDTO pregunta : preguntas) {
            String valor = parametros.get(PREFIJO_RESPUESTA + pregunta.getCodigoPregunta());
            if (valor != null && valor.matches("\\d+")) {
                respuestas.put(pregunta.getCodigoPregunta(), Integer.parseInt(valor));
            }
        }
        return respuestas;
    }

    private void cargarIngreso(Model model, LoginCandidatoEntradaDTO datos) {
        model.addAttribute("datos", datos);
        model.addAttribute("tiposDocumento", TIPODOCUMENTO.values());
    }

    private static String formatearTiempo(long segundos) {
        return String.format("%02d:%02d:%02d", segundos / 3600, (segundos % 3600) / 60, segundos % 60);
    }

    private void limpiarSesion(HttpSession session) {
        for (String atributo : List.of(SESION_CLAVE, SESION_CUESTIONARIO, SESION_BLOQUE, SESION_LIMITE)) {
            session.removeAttribute(atributo);
        }
    }
}
