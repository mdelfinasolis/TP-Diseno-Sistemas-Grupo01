package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.OpcionRespuesta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OpcionRespuestaDAO implements IDAO<OpcionRespuesta, String> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void guardar(OpcionRespuesta entidad) {
        em.persist(entidad);
    }

    @Override
    public void modificar(OpcionRespuesta entidad) {
        em.merge(entidad);
    }

    @Override
    public void eliminarLogico(String nombre) {
        OpcionRespuesta opcion = em.find(OpcionRespuesta.class, nombre);
        if (opcion != null) {
            opcion.setEliminada(true);
        }
    }

    @Override
    public OpcionRespuesta buscarPorId(String nombre) {
        return em.find(OpcionRespuesta.class, nombre);
    }

    @Override
    public List<OpcionRespuesta> buscarTodos() {
        return em.createQuery("SELECT o FROM OpcionRespuesta o ORDER BY o.nombre", OpcionRespuesta.class)
                .getResultList();
    }
}
