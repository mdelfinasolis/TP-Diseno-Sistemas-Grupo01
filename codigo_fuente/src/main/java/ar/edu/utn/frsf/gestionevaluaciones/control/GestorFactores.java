package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.FactorDAO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroFactorEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.FactorListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Factor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GestorFactores {

    private final FactorDAO factorDAO;

    public GestorFactores(FactorDAO factorDAO) {
        this.factorDAO = factorDAO;
    }

    /** CU020: factores de la competencia seleccionada (lista desplegable dependiente). */
    public List<FactorListadoDTO> buscarFactores(FiltroFactorEntradaDTO filtros) {
        return factorDAO.buscarPorFiltros(filtros.getCodigoCompetencia(), filtros.getNombre()).stream()
                .map(f -> new FactorListadoDTO(f.getCodigo(), f.getNombre(), f.getCompetencia().getNombre()))
                .toList();
    }

    public Factor obtenerFactor(String codigo) {
        return factorDAO.buscarPorId(codigo);
    }
}
