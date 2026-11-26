package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.CuestionarioDAO;
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
import ar.edu.utn.frsf.gestionevaluaciones.entity.Candidato;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Competencia;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Cuestionario;
import ar.edu.utn.frsf.gestionevaluaciones.entity.DetalleCuestionario;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Evaluacion;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Factor;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ItemOpcion;
import ar.edu.utn.frsf.gestionevaluaciones.entity.OpcionRespuesta;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Pregunta;
import ar.edu.utn.frsf.gestionevaluaciones.entity.PreguntaCuestionario;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Puesto;
import ar.edu.utn.frsf.gestionevaluaciones.entity.PuestoCompetencia;
import ar.edu.utn.frsf.gestionevaluaciones.entity.RespuestaSeleccionada;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ResultadoCompetencia;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ResultadoFactor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class GestorCuestionarios {

    /** Letras y dígitos para las claves (sin 0/O ni 1/I, que se confunden al leerlas). */
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String DIGITOS = "23456789";
    private static final int PREGUNTAS_POR_FACTOR = 2;

    private final CuestionarioDAO cuestionarioDAO;
    private final GestorPreguntas gestorPreguntas;
    private final SecureRandom aleatorio = new SecureRandom();

    // Repositorio de configuraciones (application.yml)
    @Value("${app.cuestionario.tiempo-maximo-minutos}")
    private int tiempoMaximoMinutos;
    @Value("${app.cuestionario.cantidad-maxima-accesos}")
    private int cantidadMaximaAccesos;
    @Value("${app.cuestionario.dias-plazo-activo}")
    private int diasPlazoActivo;
    @Value("${app.cuestionario.longitud-clave}")
    private int longitudClave;

    public GestorCuestionarios(CuestionarioDAO cuestionarioDAO, GestorPreguntas gestorPreguntas) {
        this.cuestionarioDAO = cuestionarioDAO;
        this.gestorPreguntas = gestorPreguntas;
    }

    // =====================================================================
    // CU025 - Evaluar candidatos
    // =====================================================================

    public List<Cuestionario> buscarCuestionariosActivos(List<Integer> nrosCandidatos) {
        return cuestionarioDAO.buscarActivosOEnProcesoPorCandidatos(nrosCandidatos);
    }

    /** Clave aleatoria de longitud configurable, con letras y dígitos, que no esté usada por otro cuestionario. */
    public String generarClave() {
        String clave;
        do {
            List<Character> caracteres = new ArrayList<>();
            caracteres.add(LETRAS.charAt(aleatorio.nextInt(LETRAS.length())));
            caracteres.add(DIGITOS.charAt(aleatorio.nextInt(DIGITOS.length())));
            String todos = LETRAS + DIGITOS;
            while (caracteres.size() < longitudClave) {
                caracteres.add(todos.charAt(aleatorio.nextInt(todos.length())));
            }
            Collections.shuffle(caracteres, aleatorio);
            StringBuilder sb = new StringBuilder();
            caracteres.forEach(sb::append);
            clave = sb.toString();
        } while (cuestionarioDAO.buscarPorClave(clave) != null);
        return clave;
    }

    /** Crea el cuestionario en estado ACTIVO, con el tiempo máximo tomado del repositorio de configuraciones. */
    @Transactional
    public Cuestionario generarCuestionario(Evaluacion evaluacion, Candidato candidato, String clave) {
        Cuestionario cuestionario = new Cuestionario(evaluacion, candidato, clave, tiempoMaximoMinutos);
        cuestionarioDAO.guardar(cuestionario);
        return cuestionario;
    }

    public List<Cuestionario> buscarPorEvaluacion(int idEvaluacion) {
        return cuestionarioDAO.buscarPorEvaluacion(idEvaluacion);
    }

    public Cuestionario obtenerPorClave(String clave) {
        return cuestionarioDAO.buscarPorClave(clave);
    }

    // =====================================================================
    // CU026 - Completar cuestionario
    // =====================================================================

    /**
     * Ingreso del candidato (CU001 3.A.2) y validaciones de acceso: cantidad de accesos,
     * tiempo máximo (EN_PROCESO) y plazo para comenzar (ACTIVO).
     */
    @Transactional
    public CuestionarioSalidaDTO obtenerCuestionario(LoginCandidatoEntradaDTO datos) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorClave(datos.getClaveIngreso());

        boolean documentoCoincide = false;
        if (cuestionario != null) {
            Candidato candidato = cuestionario.getCandidato();
            documentoCoincide = candidato.coincideDocumento(datos.getTipoDocumento(), datos.getNroDocumento());
        }
        if (cuestionario == null || !documentoCoincide || !estaActivoOEnProceso(cuestionario)) {
            return CuestionarioSalidaDTO.invalido();
        }

        LocalDateTime ahora = LocalDateTime.now();
        DetalleCuestionario detalle = cuestionario.getDetalle();
        detalle.registrarAcceso(ahora);

        boolean superoAccesos = superoMaximoAccesos(cuestionario);
        boolean vencido = estaVencido(cuestionario);
        ESTADOCUESTIONARIO estado = detalle.getEstado();

        if (superoAccesos || (vencido && estado == ESTADOCUESTIONARIO.EN_PROCESO)) {
            detalle.setEstado(ESTADOCUESTIONARIO.INCOMPLETO);
            detalle.registrarFin(ahora);
            cuestionarioDAO.modificar(cuestionario);
            // segundosRestantes = 0 indica que venció el tiempo; > 0, que se superaron los accesos
            return salida(cuestionario, 0, vencido ? 0 : Math.max(1, segundosRestantes(cuestionario, ahora)));
        }
        if (vencido && estado == ESTADOCUESTIONARIO.ACTIVO) {
            detalle.setEstado(ESTADOCUESTIONARIO.SIN_CONTESTAR);
            detalle.registrarFin(ahora);
            cuestionarioDAO.modificar(cuestionario);
            return salida(cuestionario, 0, 0);
        }

        cuestionarioDAO.modificar(cuestionario);
        if (estado == ESTADOCUESTIONARIO.EN_PROCESO) {
            return salida(cuestionario, primerBloquePendiente(cuestionario), segundosRestantes(cuestionario, ahora));
        }
        return salida(cuestionario, 1, segundosRestantes(cuestionario, ahora));
    }

    /**
     * Paso 3 del CU026: asigna al azar 2 preguntas por factor (de los factores con al menos 2 preguntas),
     * las mezcla, las agrupa en bloques y pasa el cuestionario a EN_PROCESO.
     */
    @Transactional
    public void iniciarCuestionario(String claveCuestionario) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorClave(claveCuestionario);
        if (cuestionario == null || cuestionario.getDetalle().getEstado() != ESTADOCUESTIONARIO.ACTIVO) {
            return; // ya iniciado (por ejemplo, doble click en Aceptar): no se reasignan preguntas
        }

        Evaluacion evaluacion = cuestionario.getEvaluacion();
        Puesto puesto = evaluacion.getPuesto();
        int cantPreguntasPorBloque = Math.max(1, evaluacion.getCantPreguntasPorBloque());

        List<Pregunta> elegidas = new ArrayList<>();
        for (PuestoCompetencia puestoCompetencia : puesto.getCompetencias()) {
            Competencia competencia = puestoCompetencia.getCompetencia();
            for (Factor factor : competencia.getFactores()) {
                if (factor.isEliminado()) {
                    continue;
                }
                List<Pregunta> preguntas = gestorPreguntas.buscarPorFactor(factor.getCodigo());
                if (preguntas.size() >= PREGUNTAS_POR_FACTOR) {
                    elegidas.addAll(seleccionarPreguntasAleatorias(preguntas, PREGUNTAS_POR_FACTOR));
                }
            }
        }

        // Se mezclan las preguntas de todos los factores y competencias y se agrupan en bloques
        Collections.shuffle(elegidas, aleatorio);
        for (int i = 0; i < elegidas.size(); i++) {
            int orden = i + 1;
            int nroBloque = i / cantPreguntasPorBloque + 1;
            PreguntaCuestionario preguntaCuestionario = new PreguntaCuestionario(elegidas.get(i), orden, nroBloque);
            cuestionario.agregarPreguntaCuestionario(preguntaCuestionario);
        }

        LocalDateTime ahora = LocalDateTime.now();
        DetalleCuestionario detalle = cuestionario.getDetalle();
        detalle.registrarInicio(ahora);
        detalle.setEstado(ESTADOCUESTIONARIO.EN_PROCESO);
        cuestionarioDAO.modificar(cuestionario);
    }

    public BloqueCuestionarioDTO obtenerBloque(ObtenerBloqueCuestionarioEntradaDTO datos) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorClave(datos.getClaveCuestionario());
        List<PreguntaCuestionario> preguntas = cuestionario.getPreguntasBloque(datos.getNroBloque());
        return new BloqueCuestionarioDTO(datos.getNroBloque(), preguntas.size(), cantidadBloques(cuestionario));
    }

    /** Preguntas del bloque, tomadas de las PreguntaCuestionario asignadas al iniciar (no de PreguntaDAO). */
    public List<PreguntaCuestionarioDTO> listarPreguntasBloque(ObtenerBloqueCuestionarioEntradaDTO datos) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorClave(datos.getClaveCuestionario());
        List<PreguntaCuestionarioDTO> resultado = new ArrayList<>();
        for (PreguntaCuestionario preguntaCuestionario : cuestionario.getPreguntasBloque(datos.getNroBloque())) {
            Pregunta pregunta = preguntaCuestionario.getPregunta();
            resultado.add(new PreguntaCuestionarioDTO(pregunta.getCodigo(), pregunta.getEnunciado()));
        }
        return resultado;
    }

    public List<OpcionCuestionarioDTO> listarOpcionesPregunta(String codigoPregunta) {
        Pregunta pregunta = gestorPreguntas.obtenerPregunta(codigoPregunta);
        OpcionRespuesta opcionRespuesta = pregunta.getOpcionRespuesta();
        return opcionRespuesta.getItems().stream()
                .map(item -> new OpcionCuestionarioDTO(item.getOrden(), item.getDescripcion()))
                .toList();
    }

    /** Antes de guardar cada bloque: si venció el tiempo máximo el cuestionario pasa a INCOMPLETO. */
    @Transactional
    public CuestionarioSalidaDTO verificarVigencia(String claveCuestionario) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorClave(claveCuestionario);
        LocalDateTime ahora = LocalDateTime.now();
        if (estaVencido(cuestionario)) {
            DetalleCuestionario detalle = cuestionario.getDetalle();
            detalle.setEstado(ESTADOCUESTIONARIO.INCOMPLETO);
            detalle.registrarFin(ahora);
            cuestionarioDAO.modificar(cuestionario);
            return salida(cuestionario, 0, 0);
        }
        return salida(cuestionario, primerBloquePendiente(cuestionario), segundosRestantes(cuestionario, ahora));
    }

    /** Registra las respuestas del bloque. Solo se acepta el primer bloque pendiente (no se puede volver atrás). */
    @Transactional
    public void guardarBloque(GuardarBloqueCuestionarioEntradaDTO datos) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorClave(datos.getClaveCuestionario());
        if (cuestionario.getDetalle().getEstado() != ESTADOCUESTIONARIO.EN_PROCESO
                || datos.getNroBloque() != primerBloquePendiente(cuestionario)) {
            throw new IllegalStateException("El bloque " + datos.getNroBloque() + " no es el bloque pendiente");
        }
        if (datos.getRespuestas().size() != cuestionario.getPreguntasBloque(datos.getNroBloque()).size()) {
            throw new IllegalArgumentException("Deben responderse todas las preguntas del bloque");
        }

        LocalDateTime ahora = LocalDateTime.now();
        for (RespuestaPreguntaEntradaDTO respuesta : datos.getRespuestas()) {
            PreguntaCuestionario preguntaCuestionario = cuestionario.getPreguntaCuestionario(respuesta.getCodigoPregunta());
            if (preguntaCuestionario == null || preguntaCuestionario.getNroBloque() != datos.getNroBloque()) {
                throw new IllegalArgumentException("La pregunta " + respuesta.getCodigoPregunta() + " no es del bloque");
            }
            Pregunta pregunta = preguntaCuestionario.getPregunta();
            OpcionRespuesta opcionRespuesta = pregunta.getOpcionRespuesta();
            ItemOpcion item = opcionRespuesta.getItem(respuesta.getOrdenItemSeleccionado());
            if (item == null) {
                throw new IllegalArgumentException("Opción inválida para la pregunta " + respuesta.getCodigoPregunta());
            }
            preguntaCuestionario.registrarRespuesta(item, ahora);
        }
        cuestionarioDAO.modificar(cuestionario);
    }

    /**
     * Algoritmo (3) del enunciado: total del factor = suma de las ponderaciones seleccionadas / 2;
     * total de la competencia = suma de los totales de sus factores / cantidad de factores evaluados.
     */
    @Transactional
    public void calcularResultados(String claveCuestionario) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorClave(claveCuestionario);
        if (!cuestionario.getResultadosCompetencia().isEmpty()) {
            return; // ya calculados
        }

        Map<Factor, Double> sumaPorFactor = new LinkedHashMap<>();
        for (PreguntaCuestionario preguntaCuestionario : cuestionario.getPreguntas()) {
            RespuestaSeleccionada respuesta = preguntaCuestionario.getRespuesta();
            Pregunta pregunta = preguntaCuestionario.getPregunta();
            double ponderacion = respuesta == null ? 0.0 : pregunta.getPonderacion(respuesta.getItem());
            Factor factor = pregunta.getFactor();
            sumaPorFactor.merge(factor, ponderacion, Double::sum);
        }

        Map<Competencia, List<Double>> totalesPorCompetencia = new LinkedHashMap<>();
        for (Map.Entry<Factor, Double> entrada : sumaPorFactor.entrySet()) {
            Factor factor = entrada.getKey();
            double total = entrada.getValue() / PREGUNTAS_POR_FACTOR;
            ResultadoFactor resultadoFactor = new ResultadoFactor(factor, total);
            cuestionario.agregarResultadoFactor(resultadoFactor);
            totalesPorCompetencia.computeIfAbsent(factor.getCompetencia(), c -> new ArrayList<>()).add(total);
        }

        for (Map.Entry<Competencia, List<Double>> entrada : totalesPorCompetencia.entrySet()) {
            List<Double> totales = entrada.getValue();
            double total = totales.stream().mapToDouble(Double::doubleValue).sum() / totales.size();
            ResultadoCompetencia resultadoCompetencia = new ResultadoCompetencia(entrada.getKey(), total);
            cuestionario.agregarResultadoCompetencia(resultadoCompetencia);
        }
        cuestionarioDAO.modificar(cuestionario);
    }

    @Transactional
    public ResultadoCuestionarioDTO finalizarCuestionario(FinalizarCuestionarioEntradaDTO datos) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorClave(datos.getClaveCuestionario());
        LocalDateTime ahora = LocalDateTime.now();
        DetalleCuestionario detalle = cuestionario.getDetalle();
        detalle.registrarFin(ahora);
        detalle.setEstado(ESTADOCUESTIONARIO.COMPLETO);
        cuestionarioDAO.modificar(cuestionario);
        return new ResultadoCuestionarioDTO(ESTADOCUESTIONARIO.COMPLETO, ahora,
                "El cuestionario ha sido completado con éxito.");
    }

    /**
     * ACTIVO: superó el plazo para comenzar (días desde la creación de la evaluación).
     * EN_PROCESO: ahora - fechaInicio &gt; tiempoMaximo del cuestionario.
     */
    private boolean estaVencido(Cuestionario cuestionario) {
        DetalleCuestionario detalle = cuestionario.getDetalle();
        LocalDateTime ahora = LocalDateTime.now();
        if (detalle.getEstado() == ESTADOCUESTIONARIO.ACTIVO) {
            LocalDateTime limite = cuestionario.getEvaluacion().getFechaCreacion().atStartOfDay().plusDays(diasPlazoActivo);
            return ahora.isAfter(limite);
        }
        if (detalle.getEstado() == ESTADOCUESTIONARIO.EN_PROCESO && detalle.getFechaInicio() != null) {
            return Duration.between(detalle.getFechaInicio(), ahora).compareTo(
                    Duration.ofMinutes(cuestionario.getTiempoMaximo())) > 0;
        }
        return false;
    }

    /** La cantidad máxima de accesos se controla solo para cuestionarios EN_PROCESO. */
    private boolean superoMaximoAccesos(Cuestionario cuestionario) {
        DetalleCuestionario detalle = cuestionario.getDetalle();
        return detalle.getEstado() == ESTADOCUESTIONARIO.EN_PROCESO
                && detalle.getCantidadAccesos() > cantidadMaximaAccesos;
    }

    private List<Pregunta> seleccionarPreguntasAleatorias(List<Pregunta> preguntas, int cantidad) {
        List<Pregunta> copia = new ArrayList<>(preguntas);
        Collections.shuffle(copia, aleatorio);
        return new ArrayList<>(copia.subList(0, Math.min(cantidad, copia.size())));
    }

    private static boolean estaActivoOEnProceso(Cuestionario cuestionario) {
        ESTADOCUESTIONARIO estado = cuestionario.getDetalle().getEstado();
        return estado == ESTADOCUESTIONARIO.ACTIVO || estado == ESTADOCUESTIONARIO.EN_PROCESO;
    }

    private static int cantidadBloques(Cuestionario cuestionario) {
        return cuestionario.getPreguntas().stream().mapToInt(PreguntaCuestionario::getNroBloque).max().orElse(0);
    }

    /** Primer bloque con alguna pregunta sin responder; cantidadBloques + 1 si ya se respondieron todos. */
    private static int primerBloquePendiente(Cuestionario cuestionario) {
        return cuestionario.getPreguntas().stream()
                .filter(pc -> pc.getRespuesta() == null)
                .mapToInt(PreguntaCuestionario::getNroBloque)
                .min()
                .orElse(cantidadBloques(cuestionario) + 1);
    }

    private static int segundosRestantes(Cuestionario cuestionario, LocalDateTime ahora) {
        long total = cuestionario.getTiempoMaximo() * 60L;
        LocalDateTime inicio = cuestionario.getDetalle().getFechaInicio();
        if (inicio == null) {
            return (int) total;
        }
        long transcurridos = Duration.between(inicio, ahora).getSeconds();
        return (int) Math.max(0, total - transcurridos);
    }

    private static CuestionarioSalidaDTO salida(Cuestionario cuestionario, int bloqueActual, int segundosRestantes) {
        Candidato candidato = cuestionario.getCandidato();
        return new CuestionarioSalidaDTO(
                candidato.getApellido() + ", " + candidato.getNombre(),
                cuestionario.getEvaluacion().getPuesto().getNombre(),
                cuestionario.getDetalle().getEstado(),
                bloqueActual,
                cuestionario.getTiempoMaximo(),
                segundosRestantes,
                true);
    }
}
