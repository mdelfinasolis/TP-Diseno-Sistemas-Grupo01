package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.EvaluacionDAO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.EvaluarCandidatosEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFactorEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFuncionEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroPuestoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.excel.ArchivoExcelGeneradoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.excel.ExportacionCandidatosExcelDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.excel.ExportarClavesExcelEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoClaveDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoEvaluadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CompetenciaRequeridaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.FactorListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.FuncionBusquedaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Candidato;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Cuestionario;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Evaluacion;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Puesto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class GestorEvaluaciones {

    private static final int MINIMO_PREGUNTAS_POR_FACTOR = 2;
    private static final DateTimeFormatter FORMATO_NOMBRE_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    private final EvaluacionDAO evaluacionDAO;
    private final GestorCandidatos gestorCandidatos;
    private final GestorPuestos gestorPuestos;
    private final GestorCuestionarios gestorCuestionarios;
    private final GestorFactores gestorFactores;
    private final GestorPreguntas gestorPreguntas;

    public GestorEvaluaciones(EvaluacionDAO evaluacionDAO, GestorCandidatos gestorCandidatos,
                              GestorPuestos gestorPuestos, GestorCuestionarios gestorCuestionarios,
                              GestorFactores gestorFactores, GestorPreguntas gestorPreguntas) {
        this.evaluacionDAO = evaluacionDAO;
        this.gestorCandidatos = gestorCandidatos;
        this.gestorPuestos = gestorPuestos;
        this.gestorCuestionarios = gestorCuestionarios;
        this.gestorFactores = gestorFactores;
        this.gestorPreguntas = gestorPreguntas;
    }

    public List<Evaluacion> buscarPorPuesto(String codigoPuesto) {
        return evaluacionDAO.buscarPorPuesto(codigoPuesto);
    }

    /** CU025 6.A: candidatos que tienen un cuestionario ACTIVO o EN_PROCESO y no pueden evaluarse nuevamente. */
    public List<CandidatoEvaluadoDTO> verificarCuestionariosActivos(List<Integer> nrosCandidatos) {
        List<Cuestionario> activos = gestorCuestionarios.buscarCuestionariosActivos(nrosCandidatos);
        return activos.stream()
                .map(Cuestionario::getCandidato)
                .map(c -> new CandidatoEvaluadoDTO(c.getApellido(), c.getNombre(), c.getTipoDocumento(),
                        c.getNroDocumento(), c.getNroCandidato(), null))
                .toList();
    }

    /** CU025 paso 7: funciones o puestos disponibles para la evaluación. */
    public List<FuncionBusquedaDTO> buscarFunciones(FiltroFuncionEntradaDTO filtros) {
        FiltroPuestoEntradaDTO filtroPuesto = new FiltroPuestoEntradaDTO(
                filtros.getCodigo(), filtros.getNombre(), filtros.getEmpresa());
        return gestorPuestos.buscarPuestos(filtroPuesto).stream()
                .map(p -> new FuncionBusquedaDTO(p.getCodigo(), p.getNombre(), p.getEmpresa()))
                .sorted(Comparator.comparing(FuncionBusquedaDTO::getNombre))
                .toList();
    }

    /** CU025 paso 9: competencias del puesto, indicando si cada una puede evaluarse. */
    public List<CompetenciaRequeridaDTO> listarCompetenciasRequeridas(String codigoPuesto) {
        List<CompetenciaRequeridaDTO> competencias = gestorPuestos.listarCompetenciasRequeridas(codigoPuesto);
        for (CompetenciaRequeridaDTO competencia : competencias) {
            List<FactorListadoDTO> factores = gestorFactores.buscarFactores(
                    new FiltroFactorEntradaDTO(competencia.getCodigoCompetencia(), ""));
            List<String> codigosFactor = factores.stream().map(FactorListadoDTO::getCodigo).toList();
            Map<String, Integer> cantidades = gestorPreguntas.contarPreguntasPorFactor(codigosFactor);
            competencia.setEvaluable(esEvaluable(factores, cantidades));
        }
        return competencias;
    }

    /** Una competencia es evaluable si tiene AL MENOS UN factor con 2 o más preguntas (enunciado, CU025 9.A). */
    private boolean esEvaluable(List<FactorListadoDTO> factores, Map<String, Integer> cantidades) {
        return factores.stream()
                .anyMatch(f -> cantidades.getOrDefault(f.getCodigo(), 0) >= MINIMO_PREGUNTAS_POR_FACTOR);
    }

    /** CU025 paso 11: una clave aleatoria (única) para cada candidato a evaluar. */
    public List<CandidatoClaveDTO> generarClaves(List<Integer> nrosCandidatos) {
        List<CandidatoClaveDTO> resultado = new ArrayList<>();
        Set<String> usadas = new HashSet<>();
        for (Integer nroCandidato : nrosCandidatos) {
            String clave;
            do {
                clave = gestorCuestionarios.generarClave();
            } while (!usadas.add(clave));
            Candidato candidato = gestorCandidatos.obtenerCandidato(nroCandidato);
            resultado.add(new CandidatoClaveDTO(candidato.getNroCandidato(), candidato.getApellido(),
                    candidato.getNombre(), candidato.getTipoDocumento(), candidato.getNroDocumento(), clave));
        }
        return resultado;
    }

    /**
     * CU025 paso 13: crea la evaluación y un cuestionario ACTIVO por candidato con la clave mostrada.
     * Devuelve el id de la evaluación creada.
     */
    @Transactional
    public int evaluarCandidatos(EvaluarCandidatosEntradaDTO datos) {
        List<Integer> nrosCandidatos = datos.getCandidatos().stream().map(CandidatoClaveDTO::getNroCandidato).toList();
        if (!verificarCuestionariosActivos(nrosCandidatos).isEmpty()) {
            // Un candidato puede tener un solo cuestionario activo (se vuelve a verificar al confirmar)
            throw new IllegalStateException("Hay candidatos con cuestionarios activos o en proceso");
        }

        Puesto puesto = gestorPuestos.obtenerPuesto(datos.getCodigoPuesto());
        if (puesto == null) {
            throw new IllegalArgumentException("No existe el puesto " + datos.getCodigoPuesto());
        }
        Evaluacion evaluacion = new Evaluacion(puesto, LocalDate.now(), datos.getCantPreguntasPorBloque());
        evaluacionDAO.guardar(evaluacion);

        for (CandidatoClaveDTO candidatoClave : datos.getCandidatos()) {
            Candidato candidato = gestorCandidatos.obtenerCandidato(candidatoClave.getNroCandidato());
            Cuestionario cuestionario = gestorCuestionarios.generarCuestionario(
                    evaluacion, candidato, candidatoClave.getClaveIngreso());
            evaluacion.getCuestionarios().add(cuestionario);
        }
        return evaluacion.getId();
    }

    /** CU025 pasos 15-17: candidatos y claves leídos de los cuestionarios persistidos de la evaluación. */
    public ExportacionCandidatosExcelDTO exportarCandidatosConClaves(ExportarClavesExcelEntradaDTO datos) {
        List<CandidatoClaveDTO> candidatos = gestorCuestionarios.buscarPorEvaluacion(datos.getIdEvaluacion()).stream()
                .map(cu -> {
                    Candidato c = cu.getCandidato();
                    return new CandidatoClaveDTO(c.getNroCandidato(), c.getApellido(), c.getNombre(),
                            c.getTipoDocumento(), c.getNroDocumento(), cu.getClave());
                })
                .sorted(Comparator.comparing(CandidatoClaveDTO::getApellido).thenComparing(CandidatoClaveDTO::getNombre))
                .toList();

        String nombreArchivo = datos.getNombreArchivo();
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            Evaluacion evaluacion = evaluacionDAO.buscarPorId(datos.getIdEvaluacion());
            nombreArchivo = LocalDateTime.now().format(FORMATO_NOMBRE_ARCHIVO) + "-"
                    + evaluacion.getPuesto().getNombre() + ".xls";
        }
        nombreArchivo = normalizarNombreArchivo(nombreArchivo);

        ArchivoExcelGeneradoDTO archivo = new ArchivoExcelGeneradoDTO(nombreArchivo, datos.getRutaDestino(),
                LocalDateTime.now(), candidatos.size(), "Archivo generado con " + candidatos.size() + " candidatos.");
        return new ExportacionCandidatosExcelDTO(archivo, candidatos);
    }

    /** Quita caracteres no válidos en nombres de archivo y asegura la extensión .xls. */
    private static String normalizarNombreArchivo(String nombre) {
        String limpio = nombre.trim().replaceAll("[\\\\/:*?\"<>|]", "");
        if (limpio.toLowerCase().endsWith(".xls")) {
            limpio = limpio.substring(0, limpio.length() - 4);
        }
        return limpio + ".xls";
    }
}
