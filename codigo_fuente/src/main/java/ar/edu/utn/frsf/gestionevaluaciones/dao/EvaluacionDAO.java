package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.Evaluacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EvaluacionDAO implements IDAO<Evaluacion, Integer> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void guardar(Evaluacion entidad) {
        em.persist(entidad);
    }

    @Override
    public void modificar(Evaluacion entidad) {
        em.merge(entidad);
    }

    /** Evaluacion no tiene atributo de baja lógica en el diagrama de clases ni en el DER. */
    @Override
    public void eliminarLogico(Integer id) {
        throw new UnsupportedOperationException("Evaluacion no admite baja lógica en el modelo actual");
    }

    @Override
    public Evaluacion buscarPorId(Integer id) {
        return em.find(Evaluacion.class, id);
    }

    @Override
    public List<Evaluacion> buscarTodos() {
        return em.createQuery("SELECT e FROM Evaluacion e ORDER BY e.fechaCreacion", Evaluacion.class)
                .getResultList();
    }

    /** Evaluaciones de un puesto (por código), de la más antigua a la más reciente. */
    public List<Evaluacion> buscarPorPuesto(String puesto) {
        return em.createQuery(
                        "SELECT e FROM Evaluacion e WHERE e.puesto.codigo = :puesto"
                                + " ORDER BY e.fechaCreacion, e.id", Evaluacion.class)
                .setParameter("puesto", puesto)
                .getResultList();
    }
}
