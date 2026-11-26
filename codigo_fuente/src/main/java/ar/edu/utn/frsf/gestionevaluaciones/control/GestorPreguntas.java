package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.PreguntaDAO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroPreguntaEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.PonderacionItemEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.PreguntaEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.MensajeSalidaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PreguntaListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Factor;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ItemOpcion;
import ar.edu.utn.frsf.gestionevaluaciones.entity.OpcionRespuesta;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Pregunta;
import ar.edu.utn.frsf.gestionevaluaciones.entity.PreguntaItemPonderacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class GestorPreguntas {

    static final int LONGITUD_NOMBRE = 100;
    static final int LONGITUD_ENUNCIADO = 500;
    static final int LONGITUD_DESCRIPCION = 500;
    static final double SUMA_PONDERACIONES = 10.0;

    private final PreguntaDAO preguntaDAO;
    private final GestorFactores gestorFactores;
    private final GestorOpcionesRespuesta gestorOpcionesRespuesta;

    public GestorPreguntas(PreguntaDAO preguntaDAO, GestorFactores gestorFactores,
                           GestorOpcionesRespuesta gestorOpcionesRespuesta) {
        this.preguntaDAO = preguntaDAO;
        this.gestorFactores = gestorFactores;
        this.gestorOpcionesRespuesta = gestorOpcionesRespuesta;
    }

    /**
     * CU020: búsqueda de preguntas. Sin filtros devuelve todas las no eliminadas.
     * La columna "Pregunta" de la grilla muestra el texto de la pregunta (enunciado).
     */
    public List<PreguntaListadoDTO> buscarPreguntas(FiltroPreguntaEntradaDTO filtros) {
        List<Pregunta> preguntas = preguntaDAO.buscarPorFiltros(
                filtros.getCodigoCompetencia(), filtros.getCodigoFactor(), filtros.getNombre());
        return preguntas.stream()
                .map(p -> new PreguntaListadoDTO(
                        p.getCodigo(),
                        p.getFactor().getCompetencia().getNombre(),
                        p.getFactor().getNombre(),
                        p.getEnunciado()))
                .toList();
    }

    /** CU021: valida, verifica que no exista en el factor y registra la nueva pregunta con sus ponderaciones. */
    @Transactional
    public MensajeSalidaDTO crearPregunta(PreguntaEntradaDTO datos) {
        // Se acumulan TODOS los errores (omisiones y longitudes, ponderaciones)
        List<String> errores = new ArrayList<>(validarDatos(datos));
        errores.addAll(validarPonderaciones(datos.getPonderaciones()));
        if (!errores.isEmpty()) {
            return new MensajeSalidaDTO(MensajeSalidaDTO.ERROR, "Datos incompletos o inválidos", null, errores);
        }

        if (preguntaDAO.existeEnFactor(datos.getCodigoFactor(), datos.getNombre(), datos.getEnunciado())) {
            return new MensajeSalidaDTO(MensajeSalidaDTO.ERROR, "La pregunta ya existe en el factor", null,
                    List.of("La pregunta ya existe en el factor: hay una pregunta con el mismo nombre "
                            + "o con el mismo texto de pregunta."));
        }

        Factor factor = gestorFactores.obtenerFactor(datos.getCodigoFactor());
        OpcionRespuesta opcionRespuesta = gestorOpcionesRespuesta.obtenerOpcionRespuesta(datos.getNombreOpcionRespuesta());
        if (factor == null || factor.isEliminado() || opcionRespuesta == null || opcionRespuesta.isEliminada()) {
            return new MensajeSalidaDTO(MensajeSalidaDTO.ERROR, "Datos inválidos", null,
                    List.of("El factor o la opción de respuesta seleccionados no existen."));
        }

        Pregunta pregunta = new Pregunta(datos.getNombre(), datos.getEnunciado(), datos.getDescripcion(),
                factor, opcionRespuesta);
        for (PonderacionItemEntradaDTO ponderacion : datos.getPonderaciones()) {
            ItemOpcion item = opcionRespuesta.getItem(ponderacion.getOrdenItem());
            if (item == null) {
                return new MensajeSalidaDTO(MensajeSalidaDTO.ERROR, "Datos inválidos", null,
                        List.of("El ítem " + ponderacion.getOrdenItem() + " no pertenece a la opción de respuesta."));
            }
            PreguntaItemPonderacion preguntaItemPonderacion =
                    new PreguntaItemPonderacion(pregunta, item, ponderacion.getPonderacion());
            pregunta.agregarPonderacion(preguntaItemPonderacion);
        }

        preguntaDAO.guardar(pregunta);

        return new MensajeSalidaDTO(MensajeSalidaDTO.EXITO, "Pregunta creada",
                "La Pregunta " + datos.getNombre() + " se ha creado correctamente. ¿Desea cargar otra?", null);
    }

    /** Obligatorios omitidos y campos que superan su longitud máxima. */
    public List<String> validarDatos(PreguntaEntradaDTO datos) {
        List<String> errores = new ArrayList<>();
        if (vacio(datos.getCodigoFactor())) {
            errores.add("Debe seleccionar la competencia y el factor.");
        }
        if (vacio(datos.getNombre())) {
            errores.add("El nombre de la pregunta es obligatorio.");
        } else if (datos.getNombre().length() > LONGITUD_NOMBRE) {
            errores.add("El nombre de la pregunta supera los " + LONGITUD_NOMBRE + " caracteres.");
        }
        if (vacio(datos.getEnunciado())) {
            errores.add("La pregunta es obligatoria.");
        } else if (datos.getEnunciado().length() > LONGITUD_ENUNCIADO) {
            errores.add("La pregunta supera los " + LONGITUD_ENUNCIADO + " caracteres.");
        }
        if (!vacio(datos.getDescripcion()) && datos.getDescripcion().length() > LONGITUD_DESCRIPCION) {
            errores.add("La descripción supera los " + LONGITUD_DESCRIPCION + " caracteres.");
        }
        if (vacio(datos.getNombreOpcionRespuesta())) {
            errores.add("Debe seleccionar la opción de respuesta.");
        }
        return errores;
    }

    /** Las ponderaciones son siempre 0 o positivas y deben sumar exactamente 10 puntos. */
    public List<String> validarPonderaciones(List<PonderacionItemEntradaDTO> ponderaciones) {
        List<String> errores = new ArrayList<>();
        if (ponderaciones == null || ponderaciones.isEmpty()) {
            return errores; // sin opción de respuesta seleccionada: lo informa validarDatos
        }
        if (ponderaciones.stream().anyMatch(p -> p.getPonderacion() < 0)) {
            errores.add("Las ponderaciones deben ser 0 o positivas.");
        }
        double suma = ponderaciones.stream().mapToDouble(PonderacionItemEntradaDTO::getPonderacion).sum();
        if (Math.abs(suma - SUMA_PONDERACIONES) > 1e-9) {
            errores.add("Las ponderaciones deben sumar 10 puntos (suman " + formatear(suma) + ").");
        }
        return errores;
    }

    /** CU025: cantidad de preguntas vigentes de cada factor. */
    public Map<String, Integer> contarPreguntasPorFactor(List<String> codigosFactor) {
        return preguntaDAO.contarPorFactores(codigosFactor);
    }

    /** CU026: preguntas vigentes del factor, para la selección aleatoria. */
    public List<Pregunta> buscarPorFactor(String codigoFactor) {
        return preguntaDAO.buscarPorFactor(codigoFactor);
    }

    public Pregunta obtenerPregunta(String codigo) {
        return preguntaDAO.buscarPorId(codigo);
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String formatear(double valor) {
        return valor == Math.rint(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }
}
