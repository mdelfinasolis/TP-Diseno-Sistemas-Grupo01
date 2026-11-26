package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.CandidatoDAO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.FiltroCandidatoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.CandidatoEvaluadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Candidato;
import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GestorCandidatos {

    private final CandidatoDAO candidatoDAO;

    public GestorCandidatos(CandidatoDAO candidatoDAO) {
        this.candidatoDAO = candidatoDAO;
    }

    /** CU025: búsqueda de candidatos (sin filtros devuelve todos los no eliminados). */
    public List<CandidatoEvaluadoDTO> buscarCandidatos(FiltroCandidatoEntradaDTO filtros) {
        return candidatoDAO.buscarPorFiltros(filtros.getApellido(), filtros.getNombre(), filtros.getNroCandidato())
                .stream()
                .map(c -> new CandidatoEvaluadoDTO(c.getApellido(), c.getNombre(), c.getTipoDocumento(),
                        c.getNroDocumento(), c.getNroCandidato(), null))
                .toList();
    }

    public Candidato obtenerCandidato(int nroCandidato) {
        return candidatoDAO.buscarPorId(nroCandidato);
    }

    /** CU001 3.A.2: candidato (no eliminado) con ese tipo y número de documento, o null. */
    public Candidato obtenerPorDocumento(TIPODOCUMENTO tipo, String nroDocumento) {
        if (tipo == null || nroDocumento == null || nroDocumento.isBlank()) {
            return null;
        }
        Candidato candidato = candidatoDAO.buscarPorTipoYNroDocumento(tipo, nroDocumento.trim().toUpperCase());
        return candidato == null || candidato.isEliminado() ? null : candidato;
    }
}
