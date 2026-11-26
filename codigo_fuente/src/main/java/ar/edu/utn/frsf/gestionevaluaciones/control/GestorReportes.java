package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFuncionEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroPuestoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.OrdenMeritoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.EvaluacionListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.OrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PuestoListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Candidato;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Cuestionario;
import ar.edu.utn.frsf.gestionevaluaciones.entity.DetalleCuestionario;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Evaluacion;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Puesto;
import ar.edu.utn.frsf.gestionevaluaciones.entity.PuestoCompetencia;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ResultadoCompetencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CU028 - Emitir orden de mérito. Según el diagrama de secuencia, GestorReportes no accede a
 * ningún DAO: obtiene puestos, evaluaciones y cuestionarios a través de los gestores correspondientes.
 */
@Service
@Transactional(readOnly = true)
public class GestorReportes {

    /** Orden en que se agrupan los cuestionarios no COMPLETO en el listado. */
    private static final List<ESTADOCUESTIONARIO> ORDEN_OTROS_ESTADOS = List.of(
            ESTADOCUESTIONARIO.INCOMPLETO,
            ESTADOCUESTIONARIO.EN_PROCESO,
            ESTADOCUESTIONARIO.ACTIVO,
            ESTADOCUESTIONARIO.SIN_CONTESTAR);

    private final GestorPuestos gestorPuestos;
    private final GestorEvaluaciones gestorEvaluaciones;
    private final GestorCuestionarios gestorCuestionarios;

    public GestorReportes(GestorPuestos gestorPuestos,
                          GestorEvaluaciones gestorEvaluaciones,
                          GestorCuestionarios gestorCuestionarios) {
        this.gestorPuestos = gestorPuestos;
        this.gestorEvaluaciones = gestorEvaluaciones;
        this.gestorCuestionarios = gestorCuestionarios;
    }

    /**
     * Busca los puestos y, por cada uno, calcula la cantidad de candidatos evaluados
     * y la cantidad de cuestionarios COMPLETO.
     */
    public List<PuestoOrdenMeritoDTO> buscarPuestosOrdenMerito(FiltroFuncionEntradaDTO filtros) {
        FiltroPuestoEntradaDTO filtroPuesto = new FiltroPuestoEntradaDTO(
                filtros.getCodigo(), filtros.getNombre(), filtros.getEmpresa());
        List<PuestoListadoDTO> puestos = gestorPuestos.buscarPuestos(filtroPuesto);

        List<PuestoOrdenMeritoDTO> resultado = new ArrayList<>();
        for (PuestoListadoDTO puesto : puestos) {
            Set<Integer> candidatos = new HashSet<>();
            int evaluacionesCompletas = 0;

            List<Evaluacion> evaluaciones = gestorEvaluaciones.buscarPorPuesto(puesto.getCodigo());
            for (Evaluacion evaluacion : evaluaciones) {
                List<Cuestionario> cuestionarios = gestorCuestionarios.buscarPorEvaluacion(evaluacion.getId());
                for (Cuestionario cuestionario : cuestionarios) {
                    candidatos.add(cuestionario.getCandidato().getNroCandidato());
                    if (cuestionario.getDetalle().getEstado() == ESTADOCUESTIONARIO.COMPLETO) {
                        evaluacionesCompletas++;
                    }
                }
            }
            resultado.add(new PuestoOrdenMeritoDTO(puesto.getCodigo(), puesto.getNombre(), puesto.getEmpresa(),
                    candidatos.size(), evaluacionesCompletas));
        }
        return resultado;
    }

    /** Evaluaciones del puesto seleccionado, con su cantidad de candidatos (Evaluacion.getCantidadCandidatos()). */
    public List<EvaluacionListadoDTO> listarEvaluacionesPuesto(String codigoPuesto) {
        return gestorEvaluaciones.buscarPorPuesto(codigoPuesto).stream()
                .map(e -> new EvaluacionListadoDTO(e.getId(), e.getFechaCreacion(), e.getCantidadCandidatos()))
                .toList();
    }

    public OrdenMeritoDTO emitirOrdenMerito(OrdenMeritoEntradaDTO datos) {
        Puesto puesto = gestorPuestos.obtenerPuesto(datos.getCodigoPuesto());
        if (puesto == null) {
            throw new IllegalArgumentException("No existe el puesto " + datos.getCodigoPuesto());
        }

        // De PuestoCompetencia se obtienen las ponderaciones mínimas exigidas para cada competencia.
        Map<String, Integer> minimos = new HashMap<>();
        for (PuestoCompetencia pc : puesto.getCompetencias()) {
            minimos.put(pc.getCompetencia().getCodigo(), pc.getPonderacionMinima());
        }

        List<Integer> idsEvaluaciones;
        if (datos.isTodasLasEvaluaciones()) {
            idsEvaluaciones = gestorEvaluaciones.buscarPorPuesto(datos.getCodigoPuesto()).stream()
                    .map(Evaluacion::getId)
                    .toList();
        } else {
            // Se utilizan los idsEvaluaciones indicados en OrdenMeritoEntradaDTO.
            idsEvaluaciones = datos.getIdsEvaluaciones();
        }

        List<CandidatoOrdenMeritoDTO> candidatos = new ArrayList<>();
        for (Integer idEvaluacion : idsEvaluaciones) {
            List<Cuestionario> cuestionarios = gestorCuestionarios.buscarPorEvaluacion(idEvaluacion);
            for (Cuestionario cuestionario : cuestionarios) {
                Candidato candidato = cuestionario.getCandidato();
                DetalleCuestionario detalle = cuestionario.getDetalle();
                ESTADOCUESTIONARIO estado = detalle.getEstado();

                Double puntaje = null;
                boolean cumpleMinimos = false;
                if (estado == ESTADOCUESTIONARIO.COMPLETO) {
                    List<ResultadoCompetencia> resultados = cuestionario.getResultadosCompetencia();
                    puntaje = calcularPuntaje(resultados, minimos);
                    cumpleMinimos = cumpleTodosLosMinimos(resultados, minimos);
                }

                candidatos.add(new CandidatoOrdenMeritoDTO(
                        candidato.getTipoDocumento(),
                        candidato.getNroDocumento(),
                        candidato.getApellido(),
                        candidato.getNombre(),
                        estado,
                        puntaje,
                        detalle.getFechaInicio(),
                        detalle.getFechaFin(),
                        detalle.getUltimoIngreso(),
                        detalle.getCantidadAccesos(),
                        cumpleMinimos));
            }
        }

        candidatos.sort(comparadorOrdenMerito());

        return new OrdenMeritoDTO(puesto.getCodigo(), puesto.getNombre(), puesto.getEmpresa().getNombre(),
                LocalDateTime.now(), candidatos);
    }

    /** Puntaje = suma de los ResultadoCompetencia de las competencias requeridas por el puesto. */
    private double calcularPuntaje(List<ResultadoCompetencia> resultados, Map<String, Integer> minimos) {
        return resultados.stream()
                .filter(r -> minimos.containsKey(r.getCompetencia().getCodigo()))
                .mapToDouble(ResultadoCompetencia::getTotal)
                .sum();
    }

    /** Cumple si tiene resultado para cada competencia del puesto y en todas alcanza la ponderación mínima. */
    private boolean cumpleTodosLosMinimos(List<ResultadoCompetencia> resultados, Map<String, Integer> minimos) {
        Map<String, Double> totales = new HashMap<>();
        for (ResultadoCompetencia r : resultados) {
            totales.put(r.getCompetencia().getCodigo(), r.getTotal());
        }
        return minimos.entrySet().stream()
                .allMatch(m -> totales.containsKey(m.getKey()) && totales.get(m.getKey()) >= m.getValue());
    }

    /**
     * 1) COMPLETO que cumple todos los mínimos, por puntaje descendente (orden de mérito).
     * 2) COMPLETO que no cumple algún mínimo, por puntaje descendente.
     * 3) Resto, agrupado por estado.
     * Desempate: apellido y nombre.
     */
    private Comparator<CandidatoOrdenMeritoDTO> comparadorOrdenMerito() {
        return Comparator
                .comparingInt(GestorReportes::grupo)
                .thenComparing(c -> c.getPuntajeObtenido() == null ? 0.0 : c.getPuntajeObtenido(),
                        Comparator.reverseOrder())
                .thenComparingInt(c -> ORDEN_OTROS_ESTADOS.indexOf(c.getEstadoCuestionario()))
                .thenComparing(CandidatoOrdenMeritoDTO::getApellido)
                .thenComparing(CandidatoOrdenMeritoDTO::getNombre);
    }

    private static int grupo(CandidatoOrdenMeritoDTO c) {
        if (c.getEstadoCuestionario() != ESTADOCUESTIONARIO.COMPLETO) {
            return 2;
        }
        return c.isCumpleMinimos() ? 0 : 1;
    }
}
