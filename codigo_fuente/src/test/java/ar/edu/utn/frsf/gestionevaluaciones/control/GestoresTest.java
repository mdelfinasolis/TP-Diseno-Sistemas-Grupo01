package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFactorEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFuncionEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroPreguntaEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.OrdenMeritoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CompetenciaListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.EvaluacionListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.FactorListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.OrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PreguntaListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GestoresTest {

    @Autowired
    private GestorCompetencias gestorCompetencias;
    @Autowired
    private GestorFactores gestorFactores;
    @Autowired
    private GestorPreguntas gestorPreguntas;
    @Autowired
    private GestorPuestos gestorPuestos;
    @Autowired
    private GestorEmpresas gestorEmpresas;
    @Autowired
    private GestorReportes gestorReportes;

    @Nested
    class CU020 {

        @Test
        void listarCompetenciasExcluyeLasEliminadas() {
            List<String> codigos = gestorCompetencias.listarCompetencias().stream()
                    .map(CompetenciaListadoDTO::getCodigo).toList();
            assertThat(codigos).containsExactlyInAnyOrder("C01", "C02", "C03", "C04", "C05", "C06", "C07");
        }

        @Test
        void buscarFactoresDeUnaCompetenciaExcluyeLosEliminados() {
            List<String> codigos = gestorFactores.buscarFactores(new FiltroFactorEntradaDTO("C02", "")).stream()
                    .map(FactorListadoDTO::getCodigo).toList();
            assertThat(codigos).containsExactly("F03", "F04");
        }

        @Test
        void sinFiltrosDevuelveTodasLasPreguntasNoEliminadas() {
            List<PreguntaListadoDTO> preguntas = gestorPreguntas.buscarPreguntas(new FiltroPreguntaEntradaDTO());
            assertThat(preguntas).hasSize(28);
            assertThat(preguntas).extracting(PreguntaListadoDTO::getCodigo).doesNotContain("P099");
        }

        @Test
        void filtraPorCompetenciaFactorYNombre() {
            assertThat(gestorPreguntas.buscarPreguntas(new FiltroPreguntaEntradaDTO("C01", null, null))).hasSize(5);
            assertThat(gestorPreguntas.buscarPreguntas(new FiltroPreguntaEntradaDTO("C01", "F01", null))).hasSize(3);

            List<PreguntaListadoDTO> conEquipo = gestorPreguntas.buscarPreguntas(
                    new FiltroPreguntaEntradaDTO(null, null, "EQUIPO"));
            assertThat(conEquipo).extracting(PreguntaListadoDTO::getCodigo).containsExactlyInAnyOrder("P004", "P005");
            assertThat(conEquipo.get(0).getCompetencia()).isEqualTo("LIDERAZGO");
            assertThat(conEquipo.get(0).getFactor()).isEqualTo("MOTIVACIÓN DEL EQUIPO");
            assertThat(conEquipo.get(0).getPregunta()).startsWith("¿");
        }

        @Test
        void sinResultados() {
            assertThat(gestorPreguntas.buscarPreguntas(new FiltroPreguntaEntradaDTO(null, null, "NO EXISTE"))).isEmpty();
        }
    }

    @Nested
    class CU028 {

        @Test
        void listasDesplegables() {
            assertThat(gestorPuestos.listarPuestos()).extracting("codigo")
                    .containsExactlyInAnyOrder("1515", "4845", "2147", "7845", "3030", "6060");
            assertThat(gestorEmpresas.listarEmpresas()).hasSize(3);
        }

        @Test
        void buscarPuestosCalculaCandidatosYEvaluacionesCompletas() {
            List<PuestoOrdenMeritoDTO> puestos = gestorReportes.buscarPuestosOrdenMerito(new FiltroFuncionEntradaDTO());
            assertThat(puestos).hasSize(6);
            PuestoOrdenMeritoDTO programador = buscar(puestos, "1515");
            assertThat(programador.getCantidadCandidatos()).isEqualTo(10);
            assertThat(programador.getEvaluacionesCompletas()).isEqualTo(6);
            PuestoOrdenMeritoDTO soporte = buscar(puestos, "7845");
            assertThat(soporte.getCantidadCandidatos()).isZero();
            assertThat(soporte.getEvaluacionesCompletas()).isZero();
        }

        @Test
        void buscarPuestosConFiltros() {
            assertThat(gestorReportes.buscarPuestosOrdenMerito(new FiltroFuncionEntradaDTO("15", null, null)))
                    .extracting(PuestoOrdenMeritoDTO::getCodigo).containsExactly("1515");
            assertThat(gestorReportes.buscarPuestosOrdenMerito(new FiltroFuncionEntradaDTO(null, null, "FOLDER IT")))
                    .hasSize(2);
            assertThat(gestorReportes.buscarPuestosOrdenMerito(
                    new FiltroFuncionEntradaDTO(null, "JEFE DE PROYECTO", null))).hasSize(1);
        }

        @Test
        void puestoSinEvaluaciones() {
            assertThat(gestorReportes.listarEvaluacionesPuesto("7845")).isEmpty();
        }

        @Test
        void evaluacionesDelPuesto() {
            List<EvaluacionListadoDTO> evaluaciones = gestorReportes.listarEvaluacionesPuesto("1515");
            assertThat(evaluaciones).extracting(EvaluacionListadoDTO::getIdEvaluacion).containsExactly(1, 2);
            assertThat(evaluaciones).extracting(EvaluacionListadoDTO::getCantidadCandidatos).containsExactly(5, 5);
        }

        @Test
        void ordenDeMeritoConTodasLasEvaluaciones() {
            OrdenMeritoDTO orden = gestorReportes.emitirOrdenMerito(new OrdenMeritoEntradaDTO("1515", null, true));

            assertThat(orden.getNombrePuesto()).isEqualTo("PROGRAMADOR");
            assertThat(orden.getEmpresa()).isEqualTo("FOLDER IT");
            assertThat(orden.getCandidatos()).extracting(CandidatoOrdenMeritoDTO::getApellido).containsExactly(
                    // COMPLETO que cumplen, por puntaje (PÉREZ y RODRÍGUEZ empatan en 24)
                    "PÉREZ", "RODRÍGUEZ", "GÓMEZ",
                    // COMPLETO que no cumplen, por puntaje
                    "FERNÁNDEZ", "BENÍTEZ", "SÁNCHEZ",
                    // resto agrupado por estado
                    "LÓPEZ", "MARTÍNEZ", "ROMERO", "TORRES");

            CandidatoOrdenMeritoDTO perez = orden.getCandidatos().get(0);
            assertThat(perez.isCumpleMinimos()).isTrue();
            assertThat(perez.getPuntajeObtenido()).isEqualTo(24.0);

            CandidatoOrdenMeritoDTO fernandez = orden.getCandidatos().get(3);
            assertThat(fernandez.isCumpleMinimos()).isFalse();
            assertThat(fernandez.getPuntajeObtenido()).isEqualTo(22.0);

            CandidatoOrdenMeritoDTO lopez = orden.getCandidatos().get(6);
            assertThat(lopez.getEstadoCuestionario()).isEqualTo(ESTADOCUESTIONARIO.INCOMPLETO);
            assertThat(lopez.getPuntajeObtenido()).isNull();
            assertThat(lopez.getFechaInicio()).isNotNull();
            assertThat(lopez.getUltimoIngreso()).isNotNull();
            assertThat(lopez.getCantidadAccesos()).isEqualTo(3);
        }

        @Test
        void ordenDeMeritoConUnaEvaluacionEspecifica() {
            OrdenMeritoDTO orden = gestorReportes.emitirOrdenMerito(new OrdenMeritoEntradaDTO("1515", List.of(1), false));
            assertThat(orden.getCandidatos()).extracting(CandidatoOrdenMeritoDTO::getApellido)
                    .containsExactly("PÉREZ", "GÓMEZ", "FERNÁNDEZ", "LÓPEZ", "MARTÍNEZ");
        }

        @Test
        void ordenDeMeritoSinCuestionariosCompletos() {
            OrdenMeritoDTO orden = gestorReportes.emitirOrdenMerito(new OrdenMeritoEntradaDTO("2147", null, true));
            assertThat(orden.getCandidatos()).extracting(CandidatoOrdenMeritoDTO::getEstadoCuestionario)
                    .containsExactly(ESTADOCUESTIONARIO.EN_PROCESO, ESTADOCUESTIONARIO.ACTIVO);
        }

        private PuestoOrdenMeritoDTO buscar(List<PuestoOrdenMeritoDTO> puestos, String codigo) {
            return puestos.stream().filter(p -> p.getCodigo().equals(codigo)).findFirst().orElseThrow();
        }
    }
}
