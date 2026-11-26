package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.EmpresaDAO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.EmpresaListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Empresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GestorEmpresas {

    private final EmpresaDAO empresaDAO;

    public GestorEmpresas(EmpresaDAO empresaDAO) {
        this.empresaDAO = empresaDAO;
    }

    /** CU028: carga de la lista desplegable de empresas. */
    public List<EmpresaListadoDTO> listarEmpresas() {
        return empresaDAO.buscarTodos().stream()
                .map(e -> new EmpresaListadoDTO(e.getId(), e.getNombre()))
                .toList();
    }

    public Empresa obtenerEmpresa(int id) {
        return empresaDAO.buscarPorId(id);
    }
}
