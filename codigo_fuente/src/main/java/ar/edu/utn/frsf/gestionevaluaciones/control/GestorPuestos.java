package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.PuestoDAO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroPuestoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CompetenciaRequeridaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.PuestoListadoDTO;
import java.util.Comparator;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Puesto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GestorPuestos {

    private final PuestoDAO puestoDAO;

    public GestorPuestos(PuestoDAO puestoDAO) {
        this.puestoDAO = puestoDAO;
    }

    /** CU028: carga de la lista desplegable de nombres de puesto (solo no eliminados). */
    public List<PuestoListadoDTO> listarPuestos() {
        return puestoDAO.buscarTodos().stream()
                .filter(p -> !p.isEliminado())
                .map(this::aListadoDTO)
                .toList();
    }

    public List<PuestoListadoDTO> buscarPuestos(FiltroPuestoEntradaDTO filtros) {
        return puestoDAO.buscarPorFiltros(filtros.getCodigo(), filtros.getNombre(), filtros.getEmpresa()).stream()
                .map(this::aListadoDTO)
                .toList();
    }

    public Puesto obtenerPuesto(String codigo) {
        return puestoDAO.buscarPorId(codigo);
    }

    /** CU025: competencias requeridas por el puesto con su ponderación mínima (evaluable se calcula después). */
    public List<CompetenciaRequeridaDTO> listarCompetenciasRequeridas(String codigoPuesto) {
        Puesto puesto = puestoDAO.buscarPorId(codigoPuesto);
        if (puesto == null) {
            return List.of();
        }
        return puesto.getCompetencias().stream()
                .map(pc -> new CompetenciaRequeridaDTO(pc.getCompetencia().getCodigo(), pc.getCompetencia().getNombre(),
                        pc.getPonderacionMinima(), false))
                .sorted(Comparator.comparing(CompetenciaRequeridaDTO::getCompetencia))
                .toList();
    }

    private PuestoListadoDTO aListadoDTO(Puesto p) {
        return new PuestoListadoDTO(p.getCodigo(), p.getNombre(), p.getEmpresa().getNombre());
    }
}
