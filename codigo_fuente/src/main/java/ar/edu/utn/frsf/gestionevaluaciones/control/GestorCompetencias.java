package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.CompetenciaDAO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CompetenciaListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Competencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GestorCompetencias {

    private final CompetenciaDAO competenciaDAO;

    public GestorCompetencias(CompetenciaDAO competenciaDAO) {
        this.competenciaDAO = competenciaDAO;
    }

    /** CU020: carga de la lista desplegable de competencias (solo las no eliminadas). */
    public List<CompetenciaListadoDTO> listarCompetencias() {
        return competenciaDAO.buscarTodos().stream()
                .filter(c -> !c.isEliminada())
                .map(c -> new CompetenciaListadoDTO(c.getCodigo(), c.getNombre()))
                .toList();
    }

    public Competencia obtenerCompetencia(String codigo) {
        return competenciaDAO.buscarPorId(codigo);
    }
}
