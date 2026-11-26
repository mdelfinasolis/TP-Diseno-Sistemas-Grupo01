package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.EvaluarCandidatosEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FinalizarCuestionarioEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.GuardarBloqueCuestionarioEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.LoginCandidatoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.ObtenerBloqueCuestionarioEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.PonderacionItemEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.PreguntaEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.RespuestaPreguntaEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.excel.ExportacionCandidatosExcelDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.excel.ExportarClavesExcelEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.BloqueCuestionarioDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoClaveDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoEvaluadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CompetenciaRequeridaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CuestionarioSalidaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.MensajeSalidaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PreguntaCuestionarioDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.ResultadoCuestionarioDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Cuestionario;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Pregunta;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ResultadoCompetencia;
import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Gestores de CU021, CU025 y CU026. Cada test se revierte al terminar (@Transactional). */
@SpringBootTest
@Transactional
class GestoresEtapa8Test {

    @Autowired
    private GestorPreguntas gestorPreguntas;
    @Autowired
    private GestorEvaluaciones gestorEvaluaciones;
    @Autowired
    private GestorCuestionarios gestorCuestionarios;

    private static PreguntaEntradaDTO preguntaValida(String nombre, String enunciado) {
        PreguntaEntradaDTO datos = new PreguntaEntradaDTO();
        datos.setNombre(nombre);
        datos.setEnunciado(enunciado);
        datos.setCodigoFactor("F01");
        datos.setNombreOpcionRespuesta("FRECUENCIA");
        List<PonderacionItemEntradaDTO> ponderaciones = new ArrayList<>();
        double[] valores = {0, 1, 2, 3, 4};
        for (int i = 0; i < valores.length; i++) {
            ponderaciones.add(new PonderacionItemEntradaDTO(i + 1, valores[i]));
        }
        datos.setPonderaciones(ponderaciones);
        return datos;
    }

    @Nested
    class CU021 {

        @Test
        void creaLaPreguntaConCodigoYPonderaciones() {
            MensajeSalidaDTO mensaje = gestorPreguntas.crearPregunta(
                    preguntaValida("LIDERAZGO SITUACIONAL", "¿ADAPTA SU ESTILO DE LIDERAZGO A CADA SITUACIÓN?"));

            assertThat(mensaje.getTipo()).isEqualTo(MensajeSalidaDTO.EXITO);
            assertThat(mensaje.getDescripcion())
                    .isEqualTo("La Pregunta LIDERAZGO SITUACIONAL se ha creado correctamente. ¿Desea cargar otra?");
            Pregunta pregunta = gestorPreguntas.obtenerPregunta("P100");
            assertThat(pregunta).isNotNull();
            assertThat(pregunta.getPonderaciones()).hasSize(5);
            assertThat(pregunta.getPonderacion(pregunta.getOpcionRespuesta().getItem(5))).isEqualTo(4.0);
        }

        @Test
        void informaTodasLasOmisiones() {
            MensajeSalidaDTO mensaje = gestorPreguntas.crearPregunta(new PreguntaEntradaDTO());
            assertThat(mensaje.getTipo()).isEqualTo(MensajeSalidaDTO.ERROR);
            assertThat(mensaje.getErrores()).containsExactly(
                    "Debe seleccionar la competencia y el factor.",
                    "El nombre de la pregunta es obligatorio.",
                    "La pregunta es obligatoria.",
                    "Debe seleccionar la opción de respuesta.");
        }

        @Test
        void informaLongitudesYPonderacionesInvalidas() {
            PreguntaEntradaDTO datos = preguntaValida("N".repeat(101), "¿" + "X".repeat(500) + "?");
            datos.setDescripcion("D".repeat(501));
            datos.getPonderaciones().get(0).setPonderacion(-1);
            MensajeSalidaDTO mensaje = gestorPreguntas.crearPregunta(datos);
            assertThat(mensaje.getErrores()).containsExactly(
                    "El nombre de la pregunta supera los 100 caracteres.",
                    "La pregunta supera los 500 caracteres.",
                    "La descripción supera los 500 caracteres.",
                    "Las ponderaciones deben ser 0 o positivas.",
                    "Las ponderaciones deben sumar 10 puntos (suman 9).");
        }

        @Test
        void rechazaPreguntaExistenteEnElFactorPorNombreOPorTexto() {
            MensajeSalidaDTO mismoNombre = gestorPreguntas.crearPregunta(
                    preguntaValida("DECISIONES BAJO PRESIÓN", "¿OTRO TEXTO DISTINTO?"));
            assertThat(mismoNombre.getTitulo()).isEqualTo("La pregunta ya existe en el factor");

            MensajeSalidaDTO mismoTexto = gestorPreguntas.crearPregunta(
                    preguntaValida("OTRO NOMBRE", "¿EVALÚA VARIAS ALTERNATIVAS ANTES DE DECIDIR?"));
            assertThat(mismoTexto.getTipo()).isEqualTo(MensajeSalidaDTO.ERROR);
        }
    }

    @Nested
    class CU025 {

        @Test
        void detectaCandidatosConCuestionariosActivosOEnProceso() {
            List<CandidatoEvaluadoDTO> noEvaluables = gestorEvaluaciones.verificarCuestionariosActivos(List.of(5, 8, 16));
            assertThat(noEvaluables).extracting(CandidatoEvaluadoDTO::getApellido)
                    .containsExactlyInAnyOrder("MARTÍNEZ", "ROMERO");
        }

        @Test
        void competenciaEvaluableSiTieneAlMenosUnFactorConDosPreguntas() {
            Map<String, Boolean> disenador = gestorEvaluaciones.listarCompetenciasRequeridas("3030").stream()
                    .collect(Collectors.toMap(CompetenciaRequeridaDTO::getCodigoCompetencia,
                            CompetenciaRequeridaDTO::isEvaluable));
            assertThat(disenador).containsEntry("C06", false).containsEntry("C03", true);

            // C07 tiene un factor con 3 preguntas y otro con 1: es evaluable
            assertThat(gestorEvaluaciones.listarCompetenciasRequeridas("6060"))
                    .allMatch(CompetenciaRequeridaDTO::isEvaluable);
        }

        @Test
        void generaClavesDeOchoCaracteresConLetrasYDigitos() {
            List<CandidatoClaveDTO> claves = gestorEvaluaciones.generarClaves(List.of(16, 17, 18));
            assertThat(claves).hasSize(3);
            assertThat(claves).extracting(CandidatoClaveDTO::getClaveIngreso)
                    .doesNotHaveDuplicates()
                    .allMatch(c -> c.matches("[A-Z0-9]{8}") && c.matches(".*[A-Z].*") && c.matches(".*\\d.*"));
        }

        @Test
        void evaluaCandidatosYExportaLasClavesGuardadas() {
            List<CandidatoClaveDTO> claves = gestorEvaluaciones.generarClaves(List.of(16, 17));
            int idEvaluacion = gestorEvaluaciones.evaluarCandidatos(new EvaluarCandidatosEntradaDTO("6060", claves, 4));

            List<Cuestionario> cuestionarios = gestorCuestionarios.buscarPorEvaluacion(idEvaluacion);
            assertThat(cuestionarios).hasSize(2)
                    .allMatch(c -> c.getDetalle().getEstado() == ESTADOCUESTIONARIO.ACTIVO && c.getTiempoMaximo() == 20);

            ExportacionCandidatosExcelDTO exportacion = gestorEvaluaciones.exportarCandidatosConClaves(
                    new ExportarClavesExcelEntradaDTO(idEvaluacion, null, null));
            assertThat(exportacion.getCandidatos()).extracting(CandidatoClaveDTO::getClaveIngreso)
                    .containsExactlyInAnyOrderElementsOf(claves.stream().map(CandidatoClaveDTO::getClaveIngreso).toList());
            assertThat(exportacion.getArchivo().getNombreArchivo())
                    .matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-ANALISTA DE DATOS\\.xls");
        }

        @Test
        void noPermiteEvaluarAUnCandidatoConCuestionarioActivo() {
            List<CandidatoClaveDTO> claves = gestorEvaluaciones.generarClaves(List.of(8));
            assertThatThrownBy(() -> gestorEvaluaciones.evaluarCandidatos(new EvaluarCandidatosEntradaDTO("6060", claves, 4)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class CU026 {

        private CuestionarioSalidaDTO ingresar(String nroDocumento, String clave) {
            return gestorCuestionarios.obtenerCuestionario(new LoginCandidatoEntradaDTO(TIPODOCUMENTO.DNI, nroDocumento, clave));
        }

        @Test
        void datosInvalidos() {
            assertThat(ingresar("42111001", "NOEXISTE").isValido()).isFalse();   // clave inexistente
            assertThat(ingresar("99999999", "ACTIVA27").isValido()).isFalse();   // documento que no coincide
            assertThat(ingresar("30111222", "K1001").isValido()).isFalse();      // cuestionario ya COMPLETO
        }

        @Test
        void activoVencidoQuedaSinContestar() {
            CuestionarioSalidaDTO salida = ingresar("42111002", "VENCID38");
            assertThat(salida.getEstado()).isEqualTo(ESTADOCUESTIONARIO.SIN_CONTESTAR);
        }

        @Test
        void enProcesoConTiempoAgotadoQuedaIncompleto() {
            CuestionarioSalidaDTO salida = ingresar("42111004", "TIEMPO52");
            assertThat(salida.getEstado()).isEqualTo(ESTADOCUESTIONARIO.INCOMPLETO);
            assertThat(salida.getSegundosRestantes()).isZero();
        }

        @Test
        void enProcesoQueSuperaLosAccesosQuedaIncompleto() {
            CuestionarioSalidaDTO salida = ingresar("42111005", "ACCESO63");
            assertThat(salida.getEstado()).isEqualTo(ESTADOCUESTIONARIO.INCOMPLETO);
            assertThat(salida.getSegundosRestantes()).isPositive();
            assertThat(gestorCuestionarios.obtenerPorClave("ACCESO63").getDetalle().getCantidadAccesos()).isEqualTo(4);
        }

        @Test
        void enProcesoSeRetomaEnElPrimerBloquePendiente() {
            CuestionarioSalidaDTO salida = ingresar("42111003", "PROCES49");
            assertThat(salida.getEstado()).isEqualTo(ESTADOCUESTIONARIO.EN_PROCESO);
            assertThat(salida.getBloqueActual()).isEqualTo(2);
            assertThat(salida.getSegundosRestantes()).isBetween(14 * 60, 15 * 60);
        }

        @Test
        void completaElCuestionarioYCalculaLosResultados() {
            CuestionarioSalidaDTO salida = ingresar("42111001", "ACTIVA27");
            assertThat(salida.getEstado()).isEqualTo(ESTADOCUESTIONARIO.ACTIVO);
            assertThat(salida.getNombreCandidato()).isEqualTo("VEGA, LAURA");

            gestorCuestionarios.iniciarCuestionario("ACTIVA27");
            Cuestionario cuestionario = gestorCuestionarios.obtenerPorClave("ACTIVA27");
            // C05: F09 (2 preguntas) y F10 (3) -> 4; C07: F13 (3) -> 2; F14 (1 pregunta) se omite
            assertThat(cuestionario.getPreguntas()).hasSize(6);
            assertThat(cuestionario.getDetalle().getEstado()).isEqualTo(ESTADOCUESTIONARIO.EN_PROCESO);

            BloqueCuestionarioDTO bloque1 = gestorCuestionarios.obtenerBloque(new ObtenerBloqueCuestionarioEntradaDTO("ACTIVA27", 1));
            assertThat(bloque1.getCantidadPreguntas()).isEqualTo(4);
            assertThat(bloque1.getCantidadBloques()).isEqualTo(2);

            // No se puede guardar un bloque que no es el pendiente
            assertThatThrownBy(() -> gestorCuestionarios.guardarBloque(
                    new GuardarBloqueCuestionarioEntradaDTO("ACTIVA27", 2, responderTodo(2))))
                    .isInstanceOf(IllegalStateException.class);

            gestorCuestionarios.guardarBloque(new GuardarBloqueCuestionarioEntradaDTO("ACTIVA27", 1, responderTodo(1)));
            gestorCuestionarios.guardarBloque(new GuardarBloqueCuestionarioEntradaDTO("ACTIVA27", 2, responderTodo(2)));
            assertThat(gestorCuestionarios.verificarVigencia("ACTIVA27").getEstado()).isEqualTo(ESTADOCUESTIONARIO.EN_PROCESO);

            gestorCuestionarios.calcularResultados("ACTIVA27");
            ResultadoCuestionarioDTO resultado =
                    gestorCuestionarios.finalizarCuestionario(new FinalizarCuestionarioEntradaDTO("ACTIVA27"));
            assertThat(resultado.getEstadoFinal()).isEqualTo(ESTADOCUESTIONARIO.COMPLETO);
            assertThat(resultado.getMensaje()).isEqualTo("El cuestionario ha sido completado con éxito.");

            // Se eligió siempre el ítem 5 (ponderación 4): factor = (4 + 4) / 2 = 4; competencia = promedio = 4
            Cuestionario completo = gestorCuestionarios.obtenerPorClave("ACTIVA27");
            assertThat(completo.getResultadosFactor()).hasSize(3).allMatch(r -> r.getTotal() == 4.0);
            assertThat(completo.getResultadosCompetencia()).hasSize(2)
                    .extracting(ResultadoCompetencia::getTotal).containsOnly(4.0);
        }

        private List<RespuestaPreguntaEntradaDTO> responderTodo(int nroBloque) {
            List<PreguntaCuestionarioDTO> preguntas = gestorCuestionarios.listarPreguntasBloque(
                    new ObtenerBloqueCuestionarioEntradaDTO("ACTIVA27", nroBloque));
            return preguntas.stream()
                    .map(p -> new RespuestaPreguntaEntradaDTO(p.getCodigoPregunta(), 5))
                    .toList();
        }
    }
}
