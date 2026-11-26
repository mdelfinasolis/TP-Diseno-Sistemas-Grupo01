package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.Competencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CompetenciaDAO implements IDAO<Competencia, String> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void guardar(Competencia entidad) {
        em.persist(entidad);
    }

    @Override
    public void modificar(Competencia entidad) {
        em.merge(entidad);
    }

    @Override
    public void eliminarLogico(String codigo) {
        Competencia competencia = em.find(Competencia.class, codigo);
        if (competencia != null) {
            competencia.setEliminada(true);
        }
    }

    @Override
    public Competencia buscarPorId(String codigo) {
        return em.find(Competencia.class, codigo);
    }

    @Override
    public List<Competencia> buscarTodos() {
        return em.createQuery("SELECT c FROM Competencia c ORDER BY c.nombre", Competencia.class)
                .getResultList();
    }
}
