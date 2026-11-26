package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.Factor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FactorDAO implements IDAO<Factor, String> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void guardar(Factor entidad) {
        em.persist(entidad);
    }

    @Override
    public void modificar(Factor entidad) {
        em.merge(entidad);
    }

    @Override
    public void eliminarLogico(String codigo) {
        Factor factor = em.find(Factor.class, codigo);
        if (factor != null) {
            factor.setEliminado(true);
        }
    }

    @Override
    public Factor buscarPorId(String codigo) {
        return em.find(Factor.class, codigo);
    }

    @Override
    public List<Factor> buscarTodos() {
        return em.createQuery("SELECT f FROM Factor f JOIN FETCH f.competencia ORDER BY f.nroOrden", Factor.class)
                .getResultList();
    }

    /**
     * Factores no eliminados filtrados por código de competencia (igualdad) y nombre ("contiene").
     * Los filtros vacíos no se aplican.
     */
    public List<Factor> buscarPorFiltros(String competencia, String nombre) {
        StringBuilder jpql = new StringBuilder(
                "SELECT f FROM Factor f JOIN FETCH f.competencia c WHERE f.eliminado = false");
        if (tieneValor(competencia)) {
            jpql.append(" AND c.codigo = :competencia");
        }
        if (tieneValor(nombre)) {
            jpql.append(" AND UPPER(f.nombre) LIKE :nombre");
        }
        jpql.append(" ORDER BY f.nroOrden, f.nombre");

        TypedQuery<Factor> query = em.createQuery(jpql.toString(), Factor.class);
        if (tieneValor(competencia)) {
            query.setParameter("competencia", competencia);
        }
        if (tieneValor(nombre)) {
            query.setParameter("nombre", "%" + nombre.toUpperCase() + "%");
        }
        return query.getResultList();
    }

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}
