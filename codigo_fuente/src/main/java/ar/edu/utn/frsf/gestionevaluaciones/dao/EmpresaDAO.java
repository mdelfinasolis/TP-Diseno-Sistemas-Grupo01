package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.Empresa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmpresaDAO implements IDAO<Empresa, Integer> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void guardar(Empresa entidad) {
        em.persist(entidad);
    }

    @Override
    public void modificar(Empresa entidad) {
        em.merge(entidad);
    }

    /** Empresa no tiene atributo "eliminada" en el diagrama de clases ni en el DER. */
    @Override
    public void eliminarLogico(Integer id) {
        throw new UnsupportedOperationException("Empresa no admite baja lógica en el modelo actual");
    }

    @Override
    public Empresa buscarPorId(Integer id) {
        return em.find(Empresa.class, id);
    }

    @Override
    public List<Empresa> buscarTodos() {
        return em.createQuery("SELECT e FROM Empresa e ORDER BY e.nombre", Empresa.class)
                .getResultList();
    }
}
